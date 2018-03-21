package at.porscheinformatik.sonarqube.licensecheck.gradle;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.maven.LicenseFinder;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GradleDependencyScanner implements Scanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(GradleDependencyScanner.class);

    private final MavenLicenseService mavenLicenseService;
    private final MavenDependencyService mavenDependencyService;

    private File projectRoot;

    public GradleDependencyScanner(MavenLicenseService mavenLicenseService, MavenDependencyService mavenDependencyService) {
        this.mavenLicenseService = mavenLicenseService;
        this.mavenDependencyService = mavenDependencyService;
    }

    public GradleDependencyScanner() {
        mavenDependencyService = null;
        mavenLicenseService = null;
    }

    @Override
    public List<Dependency> scan(File moduleDir) {
        this.projectRoot = moduleDir;

        try {
            return resolveDependenciesWithLicenses();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private List<Dependency> resolveDependenciesWithLicenses() throws Exception {
        GradlePomResolver gradlePomResolver = new GradlePomResolver(projectRoot);

        List<Model> poms = gradlePomResolver.resolvePomsOfAllDependencies();
        List<Dependency> dependencies = pomsToDependencies(poms);

        // todo: remove eventually
        if (mavenDependencyService == null || mavenLicenseService == null) {
            return dependencies;
        }

        return dependencies.stream()
            .map(this.loadLicenseFromPom(mavenLicenseService.getLicenseMap(), null, null))
            .map(this::mapMavenDependencyToLicense)
            .collect(Collectors.toList());
    }

    private List<Dependency> pomsToDependencies(List<Model> poms) {
        return poms.stream()
            .map(this::pomToDependency)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Dependency pomToDependency(Model model) {
        inheritFromParent(model);
        if (model.getGroupId() == null || model.getArtifactId() == null || model.getVersion() == null) {
            return null;
        }
        String license = "";
        String group = model.getGroupId();
        String artifact = model.getArtifactId();
        String name = group + ":" + artifact;

        // todo: how to use all licenses?
        if (model.getLicenses() != null && model.getLicenses().size() > 0) {
            license = model.getLicenses().get(0).getName();
        }

        return new Dependency(
            name,
            model.getVersion(),
            license);
    }

    private void inheritFromParent(Model pom) {
        if (pom.getGroupId() == null && pom.getParent().getGroupId() != null) {
            pom.setGroupId(pom.getParent().getGroupId());
        }
        if (pom.getVersion() == null && pom.getParent().getVersion() != null) {
            pom.setVersion(pom.getParent().getVersion());
        }
    }


    // todo: reuse methods from MavenDependencyScanner..
    private Function<Dependency, Dependency> loadLicenseFromPom(Map<Pattern, String> licenseMap, String userSettings,
                                                                String globalSettings) {
        return (Dependency dependency) ->
        {
            String path = dependency.getLocalPath();
            if (path == null) {
                return dependency;
            }

            int lastDotIndex = path.lastIndexOf('.');
            if (lastDotIndex > 0) {
                String pomPath = path.substring(0, lastDotIndex) + ".pom";
                List<License> licenses = LicenseFinder.getLicenses(new File(pomPath), userSettings, globalSettings);
                if (licenses.isEmpty()) {
                    LOGGER.info("No licenses found in dependency {}", dependency.getName());
                    return dependency;
                }

                for (License license : licenses) {
                    String licenseName = license.getName();
                    if (StringUtils.isNotBlank(licenseName)) {
                        for (Map.Entry<Pattern, String> entry : licenseMap.entrySet()) {
                            if (entry.getKey().matcher(licenseName).matches()) {
                                dependency.setLicense(entry.getValue());
                                return dependency;
                            }
                        }
                    }
                    LOGGER.info("No licenses found for '{}'", licenseName);
                }
            }

            return dependency;
        };
    }

    private Dependency mapMavenDependencyToLicense(Dependency dependency) {
        if (StringUtils.isBlank(dependency.getLicense())) {
            for (MavenDependency allowedDependency : mavenDependencyService.getMavenDependencies()) {
                String matchString = allowedDependency.getKey();
                if (dependency.getName().matches(matchString)) {
                    dependency.setLicense(allowedDependency.getLicense());
                }
            }
        }
        return dependency;
    }
}
