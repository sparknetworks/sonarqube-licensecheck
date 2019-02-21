package at.porscheinformatik.sonarqube.licensecheck.spdx;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

public class LicenseProvider {
    private static final LicenseProvider INSTANCE = new LicenseProvider();

    private static final Logger log = LoggerFactory.getLogger(LicenseProvider.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private LicensesWrapper additional = null;
    private LicensesWrapper wrapper = null;
    private Map<String, SpdxLicense> licenseMap = new HashMap<>();

    private LicenseProvider() {
        retrieveLicensesFromFile("spdx_licenses.json", licensesWrapper -> this.wrapper = licensesWrapper);
        retrieveLicensesFromFile("additional_licenses.json", licensesWrapper -> this.additional = licensesWrapper);
    }

    private void retrieveLicensesFromFile(String name, Consumer<LicensesWrapper> wrapperConsumer) {
        try (final InputStream resourceAsStream = LicenseProvider.class.getClassLoader().getResourceAsStream(name)) {
            if (resourceAsStream != null) {
                final LicensesWrapper licensesWrapper = Converter.fromJsonString(IOUtils.toString(resourceAsStream, Charset.forName("UTF-8")));
                wrapperConsumer.accept(licensesWrapper);
                // Prefer non-deprecated licenses
                final BinaryOperator<SpdxLicense> discriminator = (a, b) -> a.isDeprecatedLicenseID() ? b : a;
                licenseMap.putAll(licensesWrapper.getLicenses().stream().collect(Collectors.toMap(SpdxLicense::getName, identity(), discriminator)));
                licenseMap.putAll(licensesWrapper.getLicenses().stream().collect(Collectors.toMap(SpdxLicense::getLicenseID, identity(), discriminator)));
            }
        } catch (IOException e) {
            log.error("Could not read licenses from JSON", e);
        }
    }

    private static Optional<LicensesWrapper> wrapper(LicensesWrapper wrapper) {
        return Optional.ofNullable(wrapper);
    }


    public static List<SpdxLicense> getLicenses() {
        final List<SpdxLicense> spdxLicenses = wrapper(INSTANCE.wrapper).map(LicensesWrapper::getLicenses).orElseGet(ArrayList::new);
        spdxLicenses.addAll(wrapper(INSTANCE.additional).map(LicensesWrapper::getLicenses).orElse(Collections.emptyList()));
        return spdxLicenses;
    }

    public static String getVersion() {
        return wrapper(INSTANCE.wrapper).map(LicensesWrapper::getLicenseListVersion).orElse("");
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
