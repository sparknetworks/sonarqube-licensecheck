package at.porscheinformatik.sonarqube.licensecheck.license;

import at.porscheinformatik.sonarqube.licensecheck.spdx.LicenseProvider;
import at.porscheinformatik.sonarqube.licensecheck.spdx.SpdxLicense;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;

import java.util.List;
import java.util.stream.Collectors;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.*;
import static at.porscheinformatik.sonarqube.licensecheck.sonarqube.PropertiesScanner.retrieveStringList;

@ServerSide
@ScannerSide
public class LicenseService {

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
        return retrieveStringList(configuration, key, NAME);
    }


    public List<License> getGlobalLicenses() {
        return LicenseProvider.getLicenses().stream().map(SpdxLicense::toLicense).collect(Collectors.toList());
    }
}
