package at.porscheinformatik.sonarqube.licensecheck.maven;

import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.model.Dependency;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public class ArtifactDependencyMappingScannerTest {

    public static final String EXPECTED_SETTINGS_FILE = "settings.xml";

    @Test
    public void testLicensesAreFound() {
        File moduleDir = new File(".");


        Scanner scanner = new MavenDependencyScanner();

        List<Dependency> dependencies = scanner.scan(moduleDir);

        assertThat(dependencies.size(), greaterThan(0));

        final Map<String, Dependency> dependencyMap = dependencies.stream().collect(Collectors.toMap(Dependency::getName, it -> it, (a, b) -> a));
        assertThat(dependencyMap.get("org.codehaus.groovy:groovy-all").getLicense(), is("The Apache Software License, Version 2.0"));
        assertThat(dependencyMap.get("org.spockframework:spock-core").getLicense(), is("The Apache Software License, Version 2.0"));
    }

    @Test
    public void testNullMavenProjectDependencies() throws IOException {
        Scanner scanner = new MavenDependencyScanner();

        File moduleDir = Files.createTempDirectory("lala").toFile();
        moduleDir.deleteOnExit();
        List<Dependency> dependencies = scanner.scan(moduleDir);

        assertThat(dependencies, empty());
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
