package at.porscheinformatik.sonarqube.licensecheck.internal;

import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;

import java.util.List;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.INTERNAL_REGEX;

@ServerSide
@ScannerSide
public class InternalDependenciesService {
    private final Configuration configuration;

    public InternalDependenciesService(Configuration configuration) {
        super();
        this.configuration = configuration;
    }

    public List<String> getInternalDependencyRegexes() {
        return LicenseService.retrieveStringList(INTERNAL_REGEX, configuration);
    }
}
