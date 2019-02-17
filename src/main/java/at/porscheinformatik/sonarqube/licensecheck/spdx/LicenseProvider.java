package at.porscheinformatik.sonarqube.licensecheck.spdx;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

public class LicenseProvider {
    private static final LicenseProvider INSTANCE = new LicenseProvider();

    private static final Logger log = LoggerFactory.getLogger(LicenseProvider.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private LicensesWrapper wrapper = null;
    private Map<String, SpdxLicense> licenseMap = new HashMap<>();

    private LicenseProvider() {
        try (final InputStream resourceAsStream = LicenseProvider.class.getClassLoader().getResourceAsStream("spdx_licenses.json")) {
            if (resourceAsStream != null) {
                wrapper = Converter.fromJsonString(IOUtils.toString(resourceAsStream, Charset.forName("UTF-8")));
                licenseMap = wrapper.getLicenses().stream().collect(Collectors.toMap(SpdxLicense::getName, identity(), (a, b) -> a));
                licenseMap.putAll(wrapper.getLicenses().stream().collect(Collectors.toMap(SpdxLicense::getLicenseID, identity(), (a, b) -> a)));
            }
        } catch (IOException e) {
            log.error("Could not read licenses from JSON", e);
        }
    }

    private static Optional<LicensesWrapper> wrapper() {
        return Optional.ofNullable(INSTANCE.wrapper);
    }


    public static List<SpdxLicense> getLicenses() {
        return wrapper().map(LicensesWrapper::getLicenses).orElse(Collections.emptyList());
    }

    public static String getVersion() {
        return wrapper().map(LicensesWrapper::getLicenseListVersion).orElse("");
    }

    private static SpdxLicense clone(SpdxLicense license) {
        try {
            return MAPPER.readValue(MAPPER.writeValueAsBytes(license), SpdxLicense.class);
        } catch (IOException e) {
            log.info("Could not clone {}", license);
            return null;
        }
    }

    public static List<String> getLicenseNames() {
        return getLicenses().stream().map(SpdxLicense::getName).collect(Collectors.toList());
    }

    public static Optional<SpdxLicense> getByNameOrIdentifier(String name) {
        return Optional.ofNullable(INSTANCE.licenseMap.get(name)).map(LicenseProvider::clone);
    }
}
