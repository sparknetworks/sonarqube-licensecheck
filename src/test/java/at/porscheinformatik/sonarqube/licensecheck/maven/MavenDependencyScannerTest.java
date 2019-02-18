package at.porscheinformatik.sonarqube.licensecheck.maven;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

import at.porscheinformatik.sonarqube.licensecheck.internal.InternalDependenciesService;
import org.apache.commons.cli.ParseException;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;

public class MavenDependencyScannerTest {

    public static final String EXPECTED_SETTINGS_FILE = "settings.xml";

    @Test
    public void testLicensesAreFound() {
        File moduleDir = new File(".");

        Map<String, String> licenseMap = new HashMap<>();
        licenseMap.put(".*Apache.*2.*", "Apache-2.0");
        MavenLicenseService licenseService = Mockito.mock(MavenLicenseService.class);
        when(licenseService.getLicenseMap()).thenReturn(licenseMap);
        final InternalDependenciesService internalDependenciesService = Mockito.mock(InternalDependenciesService.class);
        when(internalDependenciesService.getInternalDependencyRegexes()).thenReturn(Collections.emptyList());
        final MavenDependencyService dependencyService = Mockito.mock(MavenDependencyService.class);
        when(dependencyService.getMavenDependencies()).thenReturn(Arrays.asList(new MavenDependency("org.apache.*", "Apache-2.0")));
        Scanner scanner = new MavenDependencyScanner(licenseService, dependencyService, internalDependenciesService);

        // -
        List<Dependency> dependencies = scanner.scan(moduleDir);

        assertThat(dependencies.size(), Matchers.greaterThan(0));

        // -
        for (Dependency dep : dependencies) {
            if ("org.apache.commons:commons-lang3".equals(dep.getName())) {
                assertThat(dep.getLicense(), is("Apache-2.0"));
            } else if ("org.codehaus.plexus:plexus-utils".equals(dep.getName())) {
                assertThat(dep.getLicense(), is("Apache-2.0"));
            }
        }
    }

    @Test
    public void testNullMavenProjectDependencies() throws IOException {
        MavenLicenseService licenseService = Mockito.mock(MavenLicenseService.class);
        MavenDependencyService dependencyService = Mockito.mock(MavenDependencyService.class);

        final InternalDependenciesService internalDependenciesService = Mockito.mock(InternalDependenciesService.class);
        when(internalDependenciesService.getInternalDependencyRegexes()).thenReturn(Collections.emptyList());
        Scanner scanner = new MavenDependencyScanner(licenseService, dependencyService, internalDependenciesService);

        File moduleDir = Files.createTempDirectory("lala").toFile();
        moduleDir.deleteOnExit();
        List<Dependency> dependencies = scanner.scan(moduleDir);

        assertThat(dependencies.size(), is(0));
    }

    @Test
    public void testUserSettingsRetrievalFromCommandLineArguments() {
        final MavenDependencyScanner.MavenSettings mavenSettings = MavenDependencyScanner.parseArguments("org.codehaus.plexus.classworlds.launcher.Launcher -B -e sonar:sonar -X --settings settings.xml -Panalysis");
        assertThat(mavenSettings.userSettings, equalTo(EXPECTED_SETTINGS_FILE));
    }

    @Test
    public void testUserSettingsRetrievalFromCommandLineArgumentsWithShortForm() {
        final MavenDependencyScanner.MavenSettings mavenSettings = MavenDependencyScanner.parseArguments("org.codehaus.plexus.classworlds.launcher.Launcher -B -e sonar:sonar -X -s settings.xml");
        assertThat(mavenSettings.userSettings, equalTo(EXPECTED_SETTINGS_FILE));
    }

    @Test
    public void testGlobalSettingsRetrievalFromCommandLineArguments() {
        final MavenDependencyScanner.MavenSettings mavenSettings = MavenDependencyScanner.parseArguments("org.codehaus.plexus.classworlds.launcher.Launcher -B -e sonar:sonar -X --global-settings settings.xml -Panalysis");
        assertThat(mavenSettings.globalSettings, equalTo(EXPECTED_SETTINGS_FILE));
    }

    @Test
    public void testGlobalSettingsRetrievalFromCommandLineArgumentsWithShortForm() {
        final MavenDependencyScanner.MavenSettings mavenSettings = MavenDependencyScanner.parseArguments("org.codehaus.plexus.classworlds.launcher.Launcher -B -e sonar:sonar -X -gs settings.xml -Panalysis");
        assertThat(mavenSettings.globalSettings, equalTo(EXPECTED_SETTINGS_FILE));
    }
}
