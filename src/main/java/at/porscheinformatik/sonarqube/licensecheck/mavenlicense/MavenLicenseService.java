package at.porscheinformatik.sonarqube.licensecheck.mavenlicense;

import at.porscheinformatik.sonarqube.licensecheck.sonarqube.PropertiesScanner;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.*;

@ServerSide
@ScannerSide
public class MavenLicenseService {
    private final Configuration configuration;

    public MavenLicenseService(Configuration configuration) {
        super();
        this.configuration = configuration;
    }

    public List<MavenLicense> getMavenLicenseList() {
        return PropertiesScanner.retrieveMapStream(configuration, LICENSE_REGEX, LICENSE, NAME_MATCHES)
            .map(item -> new MavenLicense(item.get(NAME_MATCHES), item.get(NAME_MATCHES))).collect(Collectors.toList());
    }

    public Map<String, String> getLicenseMap() {
        Map<String, String> licenseMap = new HashMap<>();
        for (MavenLicense license : getMavenLicenseList()) {
            licenseMap.put(license.getRegex(), license.getLicense());
        }
        return licenseMap;
    }
}
