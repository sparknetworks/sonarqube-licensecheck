package at.porscheinformatik.sonarqube.licensecheck.mavendependency;

import at.porscheinformatik.sonarqube.licensecheck.sonarqube.PropertiesScanner;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;

import java.util.List;
import java.util.stream.Collectors;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.*;

@ServerSide
@ScannerSide
public class MavenDependencyService {
    private final Configuration configuration;

    public MavenDependencyService(Configuration configuration) {
        super();
        this.configuration = configuration;
    }

    public List<MavenDependency> getMavenDependencies() {
        return PropertiesScanner.retrieveMapStream(configuration, MAVEN_REGEX, NAME_MATCHES, LICENSE)
            .map(item -> new MavenDependency(item.get(NAME_MATCHES), item.get(LICENSE)))
            .collect(Collectors.toList());
    }

}
