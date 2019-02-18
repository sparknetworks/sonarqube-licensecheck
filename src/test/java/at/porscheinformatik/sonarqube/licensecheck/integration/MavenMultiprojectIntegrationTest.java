package at.porscheinformatik.sonarqube.licensecheck.integration;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.ProjectResolver;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.internal.InternalDependenciesService;
import at.porscheinformatik.sonarqube.licensecheck.maven.MavenDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class MavenMultiprojectIntegrationTest {

    private static File projectRoot;

    @Before
    public void setup() throws IOException {
        final File parentFolder = new File(this.getClass().getClassLoader().getResource("maven-multiproject/pom.xml").getPath()).getParentFile();
        projectRoot = ProjectResolver.prepareProject(() -> parentFolder, (file) -> { });
    }

    @Test
    public void scan() {
        Map<String, String> licenseMap = new HashMap<>();
        licenseMap.put(".*Apache.*2.*", "Apache-2.0");
        MavenLicenseService licenseService = Mockito.mock(MavenLicenseService.class);
        when(licenseService.getLicenseMap()).thenReturn(licenseMap);
        final MavenDependencyService dependencyService = Mockito.mock(MavenDependencyService.class);
        final InternalDependenciesService internalDependenciesService = Mockito.mock(InternalDependenciesService.class);
        when(internalDependenciesService.getInternalDependencyRegexes()).thenReturn(Collections.emptyList());
        when(dependencyService.getMavenDependencies()).thenReturn(Collections.singletonList(new MavenDependency("org.apache.*", "Apache-2.0")));
        Scanner scanner = new MavenDependencyScanner(licenseService, dependencyService, internalDependenciesService);

        List<Dependency> dependencies = scanner.scan(projectRoot);

        assertThat(dependencies, hasSize(8));
        assertThat(dependencies, hasItem(
            new Dependency("org.spockframework:spock-core",
                "1.1-groovy-2.4",
                "Apache-2.0")));
    }
}
