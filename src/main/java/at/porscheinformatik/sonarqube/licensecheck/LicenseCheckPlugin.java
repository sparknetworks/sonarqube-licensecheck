package at.porscheinformatik.sonarqube.licensecheck;

import at.porscheinformatik.sonarqube.licensecheck.internal.InternalDependenciesService;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.spdx.LicenseProvider;
import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.config.PropertyFieldDefinition;
import org.sonar.api.resources.Qualifiers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.*;

public class LicenseCheckPlugin implements Plugin {


    public static final String LICENSE_CHECK = "License Check";
    public static final String NAME_REGEX = "Name Regex";
    public static final String LICENSE = "License";

    @Override
    public void define(Context context) {
        context.addExtensions(getExtensions());
    }

    private List<?> getExtensions() {
        return Collections.unmodifiableList(Arrays.asList(
            ValidateLicenses.class,
            LicenseCheckSensor.class,
            LicenseCheckMetrics.class,
            LicenseCheckPageDefinition.class,
            LicenseCheckMeasureComputer.class,
            LicenseCheckRulesDefinition.class,
            LicenseService.class,
            MavenDependencyService.class,
            MavenLicenseService.class,
            InternalDependenciesService.class,
            PropertyDefinition.builder(LicenseCheckPropertyKeys.ACTIVATION_KEY)
                .category(LICENSE_CHECK)
                .name("Activate")
                .description("Activate license check")
                .type(PropertyType.BOOLEAN)
                .defaultValue("true")
                .index(0)
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.FORBID_UNKNOWN)
                .category(LICENSE_CHECK)
                .name("Unknown license handling")
                .description("Forbid or allow unknown licenses")
                .type(PropertyType.BOOLEAN)
                .defaultValue("false")
                .index(1)
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.LICENSE_WHITELIST_KEY)
                .defaultValue("")
                .category(LICENSE_CHECK)
                .onQualifiers(Qualifiers.PROJECT)
                .name("Whitelisted licenses")
                .description("Define licenses that are allowed here either on a global or per-project basis")
                .fields(
                    PropertyFieldDefinition.build(NAME).name(LICENSE).description("Name of the license").type(PropertyType.SINGLE_SELECT_LIST).options(LicenseProvider.getLicenseNames()).build()
                )
                .index(2)
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.LICENSE_BLACKLIST_KEY)
                .defaultValue("")
                .category(LICENSE_CHECK)
                .onQualifiers(Qualifiers.PROJECT)
                .name("Blacklisted licenses")
                .description("Define licenses that are disallowed here either on a global or per-project basis")
                .fields(
                    PropertyFieldDefinition.build(NAME).name(LICENSE).description("Name of the license").type(PropertyType.SINGLE_SELECT_LIST).options(LicenseProvider.getLicenseNames()).build()
                )
                .index(3)
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.LICENSE_REGEX)
                .category(LICENSE_CHECK)
                .name("License Regex")
                .description("Regex rules matching license text provided")
                .fields(
                    PropertyFieldDefinition.build(NAME_MATCHES).name(NAME_REGEX).type(PropertyType.REGULAR_EXPRESSION).build(),
                    PropertyFieldDefinition.build(LicenseCheckPropertyKeys.LICENSE).name(LICENSE).type(PropertyType.SINGLE_SELECT_LIST).options(LicenseProvider.getLicenseNames()).build()
                )
                .index(4)
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.MAVEN_REGEX)
                .category(LICENSE_CHECK)
                .name("Maven Regex")
                .description("Regex rules matching licenses based on maven artifact")
                .fields(
                    PropertyFieldDefinition.build(NAME_MATCHES).name(NAME_REGEX).type(PropertyType.REGULAR_EXPRESSION).build(),
                    PropertyFieldDefinition.build(LicenseCheckPropertyKeys.LICENSE).name(LICENSE).type(PropertyType.SINGLE_SELECT_LIST).options(LicenseProvider.getLicenseNames()).build()
                )
                .index(5)
                .build(),
            PropertyDefinition.builder(INTERNAL_REGEX)
                .category(LICENSE_CHECK)
                .name("Internal Dependency Regex")
                .description("Regex rules matching internal artifacts")
                .fields(
                    PropertyFieldDefinition.build(NAME).name(NAME_REGEX).type(PropertyType.REGULAR_EXPRESSION).build()
                )
                .index(6)
                .build()
        )
        );
    }

}
