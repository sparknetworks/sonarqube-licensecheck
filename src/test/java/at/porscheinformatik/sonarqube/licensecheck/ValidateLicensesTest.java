package at.porscheinformatik.sonarqube.licensecheck;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.sonar.api.batch.fs.internal.DefaultInputModule;
import org.sonar.api.batch.fs.internal.DefaultInputProject;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.internal.SensorStorage;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.internal.DefaultIssue;

import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;
import org.sonar.api.internal.google.common.io.Files;
import org.sonar.api.scanner.fs.InputProject;

public class ValidateLicensesTest
{
    private static final License APACHE_LICENSE = new License("Apache-2.0", "Apache-2.0", true);
    private ValidateLicenses validateLicenses;

    @Before
    public void setup()
    {
        final LicenseService licenseService = mock(LicenseService.class);
        when(licenseService.getLicenses()).thenReturn(Arrays.asList(new License("MIT", "MIT", false),
            new License("LGPL is fantastic", "LGPL-2.0", true), APACHE_LICENSE, new License("Public Domain", "PDL", true)));
        validateLicenses = new ValidateLicenses(licenseService);
    }

    private SensorContextTester createContext()
    {
        SensorContextTester context = SensorContextTester.create(Files.createTempDir());

        return context;
    }

    @Test
    public void licenseNotAllowed()
    {
        SensorContextTester context = createContext();

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", "MIT")), context);

        final Issue issue = context.allIssues().stream().findFirst().get();
        assertThat(issue.toString(), containsString(LicenseCheckMetrics.LICENSE_CHECK_NOT_ALLOWED_LICENSE_KEY));
    }

    @Test
    public void licenseAllowed()
    {
        SensorContextTester context = createContext();

        validateLicenses.validateLicenses(
            deps(new Dependency("thing", "1.0", "Apache-2.0"), new Dependency("another", "2.0", "Apache-2.0")),
            context);
        assertThat(context.allIssues().isEmpty(), is(true));
    }

    //  (LGPL OR Apache-2.0) AND (LGPL OR Apache-2.0)    
    @Test
    public void checkSpdxOrCombination()
    {
        SensorContextTester context = createContext();

        validateLicenses.validateLicenses(deps(new Dependency("another", "2.0", "(LGPL-2.0 OR Apache-2.0 OR Public Domain)"),
            new Dependency("thing", "1.0", "(MIT OR Apache-2.0)")), context);

        assertThat(context.allIssues().isEmpty(), is(true));
    }

    @Test
    public void checkSpdxSeveralOrCombination()
    {
        SensorContextTester context = createContext();

        validateLicenses.validateLicenses(
            deps(new Dependency("thing", "1.0", "(Apache-2.0 OR MIT OR Apache-2.0 OR LGPL-2.0)")), context);

        assertThat(context.allIssues().isEmpty(), is(true));
    }

    @Test
    public void checkSpdxAndCombination()
    {
        SensorContextTester context = createContext();

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", "(LGPL-2.0 AND Apache-2.0)")), context);

        assertThat(context.allIssues().isEmpty(), is(true));
    }

    @Test
    public void checkSpdxAndCombinationNotAllowed()
    {
        SensorContextTester context = createContext();

        validateLicenses.validateLicenses(
            deps(new Dependency("another", "2.0", "LGPL-2.0"), new Dependency("thing", "1.0", "(Apache-2.0 AND MIT)")),
            context);

        assertThat(context.allIssues().toString(), containsString(LicenseCheckMetrics.LICENSE_CHECK_NOT_ALLOWED_LICENSE_KEY));
    }

    @Test
    public void checkSpdxAndCombinationNotFound()
    {
        SensorContextTester context = createContext();

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", "(Apache-2.0 AND Apache-1.1 AND Invalid-3.1)")), context);
        assertThat(context.allIssues().toString(), containsString(LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY));
    }

    //  LGPL OR Apache-2.0 AND MIT
    @Test
    public void checkSpdxOrAndCombination()
    {
        SensorContextTester context = createContext();

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", "(LGPL-2.0 OR (Apache-2.0 AND MIT))")),
            context);

        assertThat(context.allIssues().isEmpty(), is(true));
    }

    @Test
    public void licenseNull()
    {
        SensorContextTester context = createContext();;

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", null)), context);

        assertThat(context.allIssues().toString(), containsString(LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY));
    }

    @Test
    public void licenseUnknown()
    {
        SensorContextTester context = createContext();

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", "Mamamia")), context);

        assertThat(context.allIssues().toString(), containsString(LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY));
    }

    @Test
    public void getUsedLicenses()
    {
        final InputProject inputProject = mock(InputProject.class);
        assertThat(validateLicenses.getUsedLicenses(deps()).size(), is(0));

        Set<License> usedLicensesApache = validateLicenses.getUsedLicenses(
            deps(new Dependency("thing", "1.0", "Apache-2.0"), new Dependency("another", "2.0", "Apache-2.0")));

        assertThat(usedLicensesApache.size(), is(1));
        assertThat(usedLicensesApache, CoreMatchers.hasItem(APACHE_LICENSE));
    }

    private static Set<Dependency> deps(Dependency... dependencies)
    {
        final Set<Dependency> dependencySet = new HashSet<>();
        Collections.addAll(dependencySet, dependencies);
        return dependencySet;
    }
}
