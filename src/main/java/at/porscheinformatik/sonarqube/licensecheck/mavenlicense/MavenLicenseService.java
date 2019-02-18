package at.porscheinformatik.sonarqube.licensecheck.mavenlicense;

import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;

import java.util.*;
import java.util.stream.Collectors;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.*;

@ServerSide
@ScannerSide
public class MavenLicenseService {
    private final Configuration settings;

    public MavenLicenseService(Configuration settings) {
        super();
        this.settings = settings;
    }

    public List<MavenLicense> getMavenLicenseList() {
        final String[] positions = settings.getStringArray(LICENSE_REGEX);
        return Arrays.stream(positions).map(it -> {
            final Optional<String> license = settings.get(LICENSE_REGEX + "." + it + "." + LICENSE);
            final Optional<String> regex = settings.get(LICENSE_REGEX + "." + it + "." + NAME_MATCHES);
            if (license.isPresent() && regex.isPresent()) {
                return new MavenLicense(regex.get(), license.get());
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Map<String, String> getLicenseMap() {
        Map<String, String> licenseMap = new HashMap<>();
        for (MavenLicense license : getMavenLicenseList()) {
            licenseMap.put(license.getRegex(), license.getLicense());
        }
        return licenseMap;
    }
}
