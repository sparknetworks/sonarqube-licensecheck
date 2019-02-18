package at.porscheinformatik.sonarqube.licensecheck.internal;

import at.porscheinformatik.sonarqube.licensecheck.sonarqube.PropertiesScanner;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;

import java.util.List;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.INTERNAL_REGEX;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.NAME;

@ServerSide
@ScannerSide
public class InternalDependenciesService {
    private final Configuration configuration;

    public InternalDependenciesService(Configuration configuration) {
        super();
        this.configuration = configuration;
    }

    public List<String> getInternalDependencyRegexes() {
        return PropertiesScanner.retrieveStringList(configuration, INTERNAL_REGEX, NAME);
    }
}
