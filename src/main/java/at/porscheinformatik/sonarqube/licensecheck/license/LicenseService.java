package at.porscheinformatik.sonarqube.licensecheck.license;

import at.porscheinformatik.sonarqube.licensecheck.spdx.LicenseProvider;
import at.porscheinformatik.sonarqube.licensecheck.spdx.SpdxLicense;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.*;

@ServerSide
@ScannerSide
public class LicenseService {

    private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);
    private final Configuration configuration;

    public LicenseService(Configuration configuration) {
        super();
        this.configuration = configuration;
    }

    public List<License> getLicenses() {
        List<License> globalLicenses = getGlobalLicenses();

        final List<String> whitelist = retrieveLicensesList(LICENSE_WHITELIST_KEY);
        final List<String> blacklist = retrieveLicensesList(LICENSE_BLACKLIST_KEY);
        final boolean blacklistByDefault = configuration.getBoolean(LICENSE_BLACKLIST_DEFAULT_KEY).orElse(false);

        return globalLicenses.stream().peek(it -> {
            if (whitelist.contains(it.getName())) {
                it.setStatus(true);
            } else if (blacklist.contains(it.getName())) {
                it.setStatus(false);
            } else if (!blacklistByDefault) {
                it.setStatus(true);
            }
        }).collect(Collectors.toList());
    }

    private List<String> retrieveLicensesList(String key) {
        return retrieveStringList(key, configuration);
    }

    public static List<String> retrieveStringList(String key, Configuration configuration) {
        final List<String> strings = Arrays.stream(configuration.getStringArray(key))
            .map(it -> configuration
                .get(key + "." + it + "." + NAME)
                .orElse(null)
            )
            .filter(Objects::nonNull).collect(Collectors.toList());
        logger.debug("Retrieved {} strings for {}", strings, key);
        return strings;
    }

    public List<License> getGlobalLicenses() {
        return LicenseProvider.getLicenses().stream().map(SpdxLicense::toLicense).collect(Collectors.toList());
    }
}
