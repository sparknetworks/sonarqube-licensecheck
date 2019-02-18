package at.porscheinformatik.sonarqube.licensecheck.gradle.license;

import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class LicenseMatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseMatcher.class);
    private final Map<String, String> licenseMap;

    public LicenseMatcher(MavenLicenseService mavenLicenseService) {
        this.licenseMap = mavenLicenseService != null ? mavenLicenseService.getLicenseMap() : null;
    }

    public String viaLicenseMap(String licenseName) {
        String license = findLicenseFromLicenseMap(licenseName);
        if (license != null) {
            LOGGER.debug("Could match license: {}  to license {}", licenseName, license);
            return license;
        } else {
            LOGGER.debug("Could not match license: {}", licenseName);
            return licenseName;
        }
    }

    String findLicenseFromLicenseMap(String licenseName) {
        if (licenseMap == null) {
            return null;
        }
        return licenseMap.entrySet().stream()
            .filter(entry -> licenseName.matches(entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
    }
}
