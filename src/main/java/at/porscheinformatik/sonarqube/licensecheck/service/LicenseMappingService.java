package at.porscheinformatik.sonarqube.licensecheck.service;

import at.porscheinformatik.sonarqube.licensecheck.model.LicenseMapping;
import at.porscheinformatik.sonarqube.licensecheck.sonarqube.PropertiesReader;
import at.porscheinformatik.sonarqube.licensecheck.spdx.LicenseProvider;
import at.porscheinformatik.sonarqube.licensecheck.spdx.SpdxLicense;
import org.apache.commons.lang3.StringUtils;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.*;

@ServerSide
@ScannerSide
public class LicenseMappingService {

    private final Configuration configuration;

    public LicenseMappingService(Configuration configuration) {
        super();
        this.configuration = configuration;
    }

    public List<LicenseMapping> getMavenLicenseList() {
        return PropertiesReader.retrieveMapStream(configuration, LICENSE_REGEX, LICENSE, NAME_MATCHES)
            .map(item -> new LicenseMapping(item.get(NAME_MATCHES), item.get(LICENSE))).collect(Collectors.toList());
    }

    public Map<String, String> getLicenseMap() {
        Map<String, String> licenseMap = new HashMap<>();
        for (LicenseMapping license : getMavenLicenseList()) {
            licenseMap.put(license.getRegex(), license.getLicense());
        }
        return licenseMap;
    }

    public Optional<String> matchLicense(String licenseName) {
        if (StringUtils.isBlank(licenseName)) {
            return Optional.of(licenseName);
        }

        // Use primarily the SpdxLicense list to identify licenses according to names if matching completely
        final Optional<String> matchedLicense = LicenseProvider.getByNameOrIdentifier(licenseName).map(SpdxLicense::getName);

        if (matchedLicense.isPresent()) {
            return matchedLicense;
        }

        return matchFromRegex(licenseName);

    }

    private Optional<String> matchFromRegex(String licenseName) {
        return getLicenseMap().entrySet().stream()
            .filter(it -> licenseName.matches(it.getKey()))
            .map(Map.Entry::getValue).findFirst();
    }
}
