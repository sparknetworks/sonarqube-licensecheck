package at.porscheinformatik.sonarqube.licensecheck.service;

import at.porscheinformatik.sonarqube.licensecheck.model.ArtifactDependencyMapping;
import at.porscheinformatik.sonarqube.licensecheck.sonarqube.PropertiesReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.*;

@ServerSide
@ScannerSide
public class ArtifactDependencyService {
    private final Configuration configuration;
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactDependencyService.class);

    public ArtifactDependencyService(Configuration configuration) {
        super();
        this.configuration = configuration;
    }

    public List<ArtifactDependencyMapping> getMavenDependencies() {
        return PropertiesReader.retrieveMapStream(configuration, MAVEN_REGEX, NAME_MATCHES, LICENSE)
            .map(item -> new ArtifactDependencyMapping(item.get(NAME_MATCHES), item.get(LICENSE)))
            .collect(Collectors.toList());
    }

    public String byPackage(String packageName) {
        return matchByPackage(packageName)
            .orElseGet(() -> {
                LOGGER.debug("Could not find matching license for package name: {}", packageName);
                return "";
            });
    }

    public Optional<String> matchByPackage(String packageName) {
        return getMavenDependencies().stream()
            .filter(artifactDependencyMapping -> packageName.matches(artifactDependencyMapping.getKey()))
            .findAny()
            .map(ArtifactDependencyMapping::getLicense);
    }


}
