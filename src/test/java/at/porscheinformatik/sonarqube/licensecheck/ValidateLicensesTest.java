package at.porscheinformatik.sonarqube.licensecheck;

import at.porscheinformatik.sonarqube.licensecheck.model.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.model.LicenseDefinition;
import at.porscheinformatik.sonarqube.licensecheck.service.LicenseDefinitionService;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.internal.google.common.io.Files;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidateLicensesTest {
    private static final LicenseDefinition APACHE_LICENSE = new LicenseDefinition("Apache-2.0", "Apache-2.0", true);
    private ValidateLicenses validateLicenses;

    @Before
    public void setup() {
        final LicenseDefinitionService licenseDefinitionService = mock(LicenseDefinitionService.class);
        when(licenseDefinitionService.getLicenses()).thenReturn(Arrays.asList(new LicenseDefinition("MIT", "MIT", false),
            new LicenseDefinition("LGPL is fantastic", "LGPL-2.0", true), APACHE_LICENSE, new LicenseDefinition("Public Domain", "PDL", true)));
        validateLicenses = new ValidateLicenses(licenseDefinitionService);
    }

    private SensorContextTester createContext() {
        SensorContextTester context = SensorContextTester.create(Files.createTempDir());

        return context;
    }

    @Test
    public void licenseNotAllowed() {
        SensorContextTester context = createContext();

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", "MIT")), context);

        final Issue issue = context.allIssues().stream().findFirst().get();
        assertThat(issue.toString(), containsString(LicenseCheckMetrics.LICENSE_CHECK_NOT_ALLOWED_LICENSE_KEY));
    }

    @Test
    public void licenseAllowed() {
        SensorContextTester context = createContext();

        validateLicenses.validateLicenses(
            deps(new Dependency("thing", "1.0", "Apache-2.0"), new Dependency("another", "2.0", "Apache-2.0")),
            context);
        assertThat(context.allIssues(), empty());
    }

    //  (LGPL OR Apache-2.0) AND (LGPL OR Apache-2.0)    
    @Test
    public void checkSpdxOrCombination() {
        SensorContextTester context = createContext();

        validateLicenses.validateLicenses(deps(new Dependency("another", "2.0", "(LGPL-2.0 OR Apache-2.0 OR Public Domain)"),
            new Dependency("thing", "1.0", "(MIT OR Apache-2.0)")), context);

        assertThat(context.allIssues(), empty());
    }

    @Test
    public void checkSpdxSeveralOrCombination() {
        SensorContextTester context = createContext();

        validateLicenses.validateLicenses(
            deps(new Dependency("thing", "1.0", "(Apache-2.0 OR MIT OR Apache-2.0 OR LGPL-2.0)")), context);

        assertThat(context.allIssues(), empty());
    }

    @Test
    public void checkSpdxAndCombination() {
        SensorContextTester context = createContext();

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", "(LGPL-2.0 AND Apache-2.0)")), context);

        assertThat(context.allIssues(), empty());
    }

    @Test
    public void checkSpdxAndCombinationNotAllowed() {
        SensorContextTester context = createContext();

        validateLicenses.validateLicenses(
            deps(new Dependency("another", "2.0", "LGPL-2.0"), new Dependency("thing", "1.0", "(Apache-2.0 AND MIT)")),
            context);

        assertThat(context.allIssues().toString(), containsString(LicenseCheckMetrics.LICENSE_CHECK_NOT_ALLOWED_LICENSE_KEY));
    }

    @Test
    public void checkSpdxAndCombinationNotFound() {
        SensorContextTester context = createContext();

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", "(Apache-2.0 AND Apache-1.1 AND Invalid-3.1)")), context);
        assertThat(context.allIssues().toString(), containsString(LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY));
    }

    //  LGPL OR Apache-2.0 AND MIT
    @Test
    public void checkSpdxOrAndCombination() {
        SensorContextTester context = createContext();

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", "(LGPL-2.0 OR (Apache-2.0 AND MIT))")),
            context);

        assertThat(context.allIssues(), empty());
    }

    @Test
    public void licenseNull() {
        SensorContextTester context = createContext();

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", (String) null)), context);

        assertThat(context.allIssues().toString(), containsString(LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY));
    }

    @Test
    public void licenseUnknown() {
        SensorContextTester context = createContext();

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", "Mamamia")), context);

        assertThat(context.allIssues().toString(), containsString(LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY));
    }

    @Test
    public void getUsedLicenses() {
        assertThat(validateLicenses.getUsedLicenses(deps()).size(), is(0));

        Set<LicenseDefinition> usedLicensesApache = validateLicenses.getUsedLicenses(
            deps(new Dependency("thing", "1.0", "Apache-2.0"), new Dependency("another", "2.0", "Apache-2.0")));

        assertThat(usedLicensesApache.size(), is(1));
        assertThat(usedLicensesApache, CoreMatchers.hasItem(APACHE_LICENSE));
    }

    private static Set<Dependency> deps(Dependency... dependencies) {
        final Set<Dependency> dependencySet = new HashSet<>();
        Collections.addAll(dependencySet, dependencies);
        return dependencySet;
    }
}
