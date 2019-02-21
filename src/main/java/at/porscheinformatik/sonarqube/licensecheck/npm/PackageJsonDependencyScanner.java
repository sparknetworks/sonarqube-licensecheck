package at.porscheinformatik.sonarqube.licensecheck.npm;

import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.model.Dependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class PackageJsonDependencyScanner implements Scanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(PackageJsonDependencyScanner.class);
    public static final String LICENSE = "license";


    private final Set<String> transversedDependencies = new HashSet<>();

    @Override
    public List<Dependency> scan(File file) {
        File base = file;
        base = getNodeModulesFolder(file, base);
        File packageJsonFile = new File(base, "package.json");

        if (packageJsonFile.exists()) {
            try (InputStream fis = new FileInputStream(packageJsonFile);
                 JsonReader jsonReader = Json.createReader(fis)) {
                JsonObject jsonObject = jsonReader.readObject();
                JsonObject jsonObjectDependencies = jsonObject.getJsonObject("dependencies");
                if (jsonObjectDependencies != null) {
                    File nodeModulesFolder = new File(packageJsonFile.getParentFile(), "node_modules");
                    return dependencyParser(jsonObjectDependencies, new ArrayList<>(), nodeModulesFolder);
                }
            } catch (IOException e) {
                LOGGER.error("Error reading package.json", e);
            }
        }
        return emptyList();
    }

    private File getNodeModulesFolder(File file, File base) {
        try (final Stream<Path> walk = Files.walk(file.toPath())) {
            final Optional<Path> nodeModules = walk.filter(it -> it.endsWith("node_modules")).findAny();
            if (nodeModules.isPresent()) {
                LOGGER.info("Found node_modules folder at {}", nodeModules.get());
                base = nodeModules.get().toFile().getParentFile();
            }
        } catch (IOException e) {
            LOGGER.error("Error trying to find a node_modules folder", e);
        }
        return base;
    }

    private List<Dependency> dependencyParser(JsonObject jsonDependencies, List<Dependency> dependencies, File nodeModulesFolder) {
        if (nodeModulesFolder.exists() && nodeModulesFolder.isDirectory()) {
            jsonDependencies.forEach((key, value) -> moduleCheck(nodeModulesFolder, key, dependencies));
        }

        return dependencies;
    }

    private void moduleCheck(File nodeModulesFolder, String identifier, List<Dependency> dependencies) {
        File moduleFolder = new File(nodeModulesFolder, identifier);

        if (!moduleFolder.exists() || !moduleFolder.isDirectory()) {
            return;
        }
        File packageFile = new File(moduleFolder, "package.json");

        try (InputStream fis = new FileInputStream(packageFile); JsonReader jsonReader = Json.createReader(fis)) {
            JsonObject jsonObject = jsonReader.readObject();
            if (jsonObject == null) {
                return;
            }
            List<String> license = getLicenseString(jsonObject);
            final Dependency version = new Dependency(identifier, jsonObject.getString("version"), license);
            dependencies.add(version);

            JsonObject jsonObjectDependencies = jsonObject.getJsonObject("dependencies");
            // Skip parsing inner dependencies of the dependency if it already exists in the stack
            if (jsonObjectDependencies != null && !this.transversedDependencies.contains(identifier)) {
                this.transversedDependencies.add(identifier);
                dependencyParser(jsonObjectDependencies, dependencies, nodeModulesFolder);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Could not find package.json", e);
        } catch (Exception e) {
            LOGGER.error("Error adding dependency " + identifier, e);
        }
    }

    private static List<String> getLicenseString(JsonObject jsonObject) {

        if (jsonObject.containsKey(LICENSE)) {
            final JsonValue.ValueType valueType = jsonObject.get(LICENSE).getValueType();
            if (valueType == JsonValue.ValueType.STRING) {
                return singletonList(jsonObject.getString(LICENSE));
            }
            if (valueType == JsonValue.ValueType.OBJECT) {
                final JsonObject licenseObject = jsonObject.getJsonObject(LICENSE);
                if (licenseObject.containsKey("type")) {
                    return singletonList(licenseObject.getString("type"));
                } else if (licenseObject.containsKey("name")) {
                    return singletonList(licenseObject.getString("name"));
                } else {
                    LOGGER.info("Could not retrieve license from {}", licenseObject);
                }
            }
            return emptyList();
        }
        if (jsonObject.containsKey("licenses")) {
            JsonArray licenses = jsonObject.getJsonArray("licenses");
            if (licenses.isEmpty()) {
                return licenses.stream().map(JsonValue::toString).collect(Collectors.toList());
            }
        }
        return emptyList();
    }

}
