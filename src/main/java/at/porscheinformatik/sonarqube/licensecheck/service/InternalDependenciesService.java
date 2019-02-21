package at.porscheinformatik.sonarqube.licensecheck.service;

import at.porscheinformatik.sonarqube.licensecheck.sonarqube.PropertiesReader;
import org.apache.commons.lang3.StringUtils;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;

import java.util.List;
import java.util.Optional;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.INTERNAL_REGEX;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.NAME;
import static at.porscheinformatik.sonarqube.licensecheck.maven.MavenDependencyScanner.INTERNAL_LICENSE;

@ServerSide
@ScannerSide
public class InternalDependenciesService {
    private final Configuration configuration;

    public InternalDependenciesService(Configuration configuration) {
        super();
        this.configuration = configuration;
    }

    public List<String> getInternalDependencyRegexes() {
        return PropertiesReader.retrieveStringList(configuration, INTERNAL_REGEX, NAME);
    }

    public Optional<String> matchByPackage(String dependencyName) {
        if (StringUtils.isBlank(dependencyName)) {
            return Optional.empty();
        }
        return getInternalDependencyRegexes().stream().filter(dependencyName::matches).findAny().map(it -> INTERNAL_LICENSE);
    }
}
