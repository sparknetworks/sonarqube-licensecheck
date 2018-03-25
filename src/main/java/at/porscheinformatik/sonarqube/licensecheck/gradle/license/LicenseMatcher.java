package at.porscheinformatik.sonarqube.licensecheck.gradle.license;

import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import org.apache.maven.model.License;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class LicenseMatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseMatcher.class);
    private final Map<Pattern, String> licenseMap;

    public LicenseMatcher(MavenLicenseService mavenLicenseService) {
        this.licenseMap = mavenLicenseService != null ? mavenLicenseService.getLicenseMap() : null;
    }

    public String viaLicenseMap(String licenseName) {
        if (licenseMap != null) {
            for (Map.Entry<Pattern, String> entry : licenseMap.entrySet()) {
                if (entry.getKey().matcher(licenseName).matches()) {
                    return entry.getValue();
                }
            }
        }
        LOGGER.debug("Could not match license: " + licenseName);
        return "";
    }

    public Predicate<License> licenseHasMatchInLicenseMap() {
        return license -> licenseMap.entrySet().stream()
            .map(entry -> entry.getKey().matcher(license.getName()).matches())
            .findFirst()
            .orElse(false);
    }
}
