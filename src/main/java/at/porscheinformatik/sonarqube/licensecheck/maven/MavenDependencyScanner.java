package at.porscheinformatik.sonarqube.licensecheck.maven;

import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.model.Dependency;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.shared.invoker.*;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MavenDependencyScanner implements Scanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavenDependencyScanner.class);

    private static final String MAVEN_REPO_LOCAL = "maven.repo.local";
    public static final Pattern USER_SETTINGS_PATTERN = Pattern.compile("-(s|-settings)");
    public static final Pattern GLOBAL_SETTINGS_PATTERN = Pattern.compile("-(gs|-global-settings)");
    public static final String INTERNAL_LICENSE = "Internal (Own Code)";
    public static final String POM_FILENAME = "pom.xml";

    @Override
    public List<Dependency> scan(File moduleDir) {
        MavenSettings commandLineArgs = getCommandLineArgs();
        LOGGER.info("Module path is: {}", moduleDir.getPath());
        try (final Stream<Path> pathStream = Files.walk(moduleDir.toPath())) {
            List<Path> poms = pathStream.filter(path -> path.endsWith(POM_FILENAME)).collect(Collectors.toList());
            final boolean singlePom = poms.size() == 1;
            if (!singlePom) {
                final Optional<Path> path = selectParent(poms);
                if (path.isPresent()) {
                    poms = Collections.singletonList(path.get());
                }
            }
            return poms
                .stream()
                .peek(path -> LOGGER.info("Reading pom {}", path))
                .flatMap(path -> readDependencyList(path.toFile(), commandLineArgs.userSettings, commandLineArgs.globalSettings, singlePom))
                .map(this.loadLicenseFromPom(commandLineArgs.userSettings, commandLineArgs.globalSettings))
                .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Could not read all child poms of the module: ", e);
        }
        return Collections.emptyList();

    }

    private Optional<Path> selectParent(List<Path> paths) {
        final List<Model> models = paths.stream()
            .map(this::toModel).collect(Collectors.toList());
        List<String> modules = models.stream()
            .filter(Objects::nonNull).map(Model::getModules)
            .flatMap(List::stream).collect(Collectors.toList());
        return models.stream()
            .filter(it -> it.getParent() == null || !modules.contains(it.getArtifactId()))
            .findFirst().map(Model::getPomFile).map(File::toPath);
    }

    private Model toModel(Path path) {
        final File pomFile = path.toFile();
        try (final FileReader reader = new FileReader(pomFile)) {
            final Model model = new MavenXpp3Reader().read(reader);
            model.setPomFile(pomFile);
            return model;
        } catch (IOException | XmlPullParserException e) {
            LOGGER.warn("Could not read pom information");
        }
        return null;
    }

    private Stream<Dependency> readDependencyList(File moduleDir, String userSettings, String globalSettings, boolean singlePom) {
        Path tempFile = createTempFile();
        if (tempFile == null) {
            return Stream.empty();
        }

        final List<String> targets = new ArrayList<>();
        if (!singlePom) {
            targets.add("install");
        }
        targets.add("dependency:list");
        InvocationRequest request = buildInvocationRequest(moduleDir, userSettings, globalSettings, targets, properties -> {
            properties.setProperty("outputFile", tempFile.toAbsolutePath().toString());
            properties.setProperty("outputAbsoluteArtifactFilename", "true");
            properties.setProperty("includeScope", "runtime"); // only runtime (scope compile + runtime)
            properties.setProperty("appendOutput", "true");
            properties.setProperty("skipTests", "true");
        });

        Invoker invoker = new DefaultInvoker();
        invoker.setOutputHandler(LOGGER::debug); // Push maven output to debug if available
        invoker.setErrorHandler(LOGGER::error);

        try {
            LOGGER.debug("Attempting to execute {}", request);
            InvocationResult result = invoker.execute(request);
            if (result.getExitCode() != 0) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Could not get dependency list via maven", result.getExecutionException());
                } else {
                    LOGGER.warn("Could not get dependency list via maven {}", Optional.ofNullable(result.getExecutionException()).map(Exception::getMessage).orElse("[could not get error]"));
                }
            }

            return findDependenciesInFile(tempFile);
        } catch (MavenInvocationException e) {
            LOGGER.warn("Could not get dependency list via maven", e);
        } catch (Exception e) {
            LOGGER.warn("Error reading file", e);
        }
        return Stream.empty();
    }

    private Stream<Dependency> findDependenciesInFile(Path tempFile) throws IOException {
        return Files.lines(tempFile)
            .filter(StringUtils::isNotBlank)
            .map(MavenDependencyScanner::findDependency)
            .filter(Objects::nonNull);
    }

    private InvocationRequest buildInvocationRequest(File pomFile, String userSettings, String globalSettings, List<String> goals, Consumer<Properties> propertiesConsumer) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setRecursive(true);
        request.setPomFile(pomFile);
        request.setGoals(goals);
        if (userSettings != null) {
            final File userSettingsFile = new File(userSettings);
            LOGGER.debug("Attempting to use settings from {}", userSettingsFile.getAbsolutePath());
            request.setUserSettingsFile(userSettingsFile);
        }
        if (globalSettings != null) {
            final File globalSettingsFile = new File(globalSettings);
            LOGGER.debug("Attempting to use settings from {}", globalSettingsFile.getAbsolutePath());
            request.setGlobalSettingsFile(globalSettingsFile);
        }
        Properties properties = new Properties();
        request.setProperties(properties);
        if (System.getProperty(MAVEN_REPO_LOCAL) != null) {
            final File localRepository = new File(System.getProperty(MAVEN_REPO_LOCAL));
            if (localRepository.canWrite()) {
                request.setLocalRepositoryDirectory(localRepository);
            }
        }
        propertiesConsumer.accept(properties);

        return request;
    }

    private Path createTempFile() {
        try {
            Path tempFile = Files.createTempFile("dependencies", ".txt");
            tempFile.toFile().deleteOnExit();
            return tempFile;
        } catch (IOException e) {
            LOGGER.error("Could not create temp file for dependencies: {}", e.getMessage());
            return null;
        }
    }

    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile("\\s*([^:]*):([^:]*):[^:]*:([^:]*):[^:]*:(.*)");

    private static Dependency findDependency(String line) {
        Matcher matcher = DEPENDENCY_PATTERN.matcher(line);
        if (matcher.find()) {
            String groupId = matcher.group(1);
            String artifactId = matcher.group(2);
            String version = matcher.group(3);
            String path = matcher.group(4);
            Dependency dependency = new Dependency(groupId + ":" + artifactId, version, (String) null);
            dependency.setLocalPath(path);
            return dependency;
        }
        return null;
    }

    private Function<Dependency, Dependency> loadLicenseFromPom(String userSettings,
                                                                String globalSettings) {
        return (Dependency dependency) ->
            Optional.ofNullable(dependency.getLocalPath())
                .map(it -> loadLicense(userSettings, globalSettings, dependency))
                .orElse(dependency);
    }

    private Dependency loadLicense(String userSettings, String globalSettings,
                                   Dependency dependency) {
        String path = dependency.getLocalPath();
        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex > 0) {
            String pomPath = path.substring(0, lastDotIndex) + ".pom";
            List<License> licenses = LicenseFinder.getLicenses(new File(pomPath), userSettings, globalSettings);
            if (licenses.isEmpty()) {
                LOGGER.info("No licenses found in dependency {}", dependency.getName());
                return dependency;
            }

            dependency.setLicenses(licenses.stream().map(License::getName).collect(Collectors.toSet()));
        }
        return dependency;
    }

    private static MavenSettings getCommandLineArgs() {
        String commandArgs = System.getProperty("sun.java.command");
        LOGGER.debug("Retrieved command line arguments are: {}", commandArgs);
        return parseArguments(commandArgs);
    }

    static MavenSettings parseArguments(String commandArgs) {
        String[] args = commandArgs.split(" ");
        MavenSettings mavenSettings = new MavenSettings();
        for (int i = 0; i < args.length; i++) {
            if (USER_SETTINGS_PATTERN.matcher(args[i]).matches() && i + 1 < args.length) {
                mavenSettings.userSettings = args[i + 1];
            } else if (GLOBAL_SETTINGS_PATTERN.matcher(args[i]).matches() && i + 1 < args.length) {
                mavenSettings.globalSettings = args[i + 1];
            }
        }

        return mavenSettings;
    }

    static class MavenSettings {
        String userSettings;
        String globalSettings;
    }

}
