package at.porscheinformatik.sonarqube.licensecheck.integration;

import at.porscheinformatik.sonarqube.licensecheck.ProjectResolver;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.maven.MavenDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.model.Dependency;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class MavenIntegrationTest {

    private static File projectRoot;

    @Before
    public void setup() throws IOException {
        final File parentFolder = new File(this.getClass().getClassLoader().getResource("maven/pom.xml").getPath()).getParentFile();
        projectRoot = ProjectResolver.prepareProject(() -> parentFolder, (file) -> { });
    }

    @Test
    public void scan() {
        Scanner scanner = new MavenDependencyScanner();

        List<Dependency> dependencies = scanner.scan(projectRoot);

        assertThat(dependencies, hasSize(5));
        assertThat(dependencies, hasItem(
            new Dependency("org.spockframework:spock-core",
                "1.1-groovy-2.4",
                "The Apache Software License, Version 2.0")));
    }
}
