package at.porscheinformatik.sonarqube.licensecheck.gradle;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GradleDependencyScannerTest {

    private static File projectRoot;

    @Before
    public void setup() throws IOException {
        projectRoot = new File("build/testProject");
        projectRoot.mkdirs();

        File buildGradleSrc = new File(this.getClass().getClassLoader().getResource("gradle/build.gradle").getFile());
        File buildGradleTrg = new File(projectRoot, "build.gradle");
        FileUtils.copyFile(buildGradleSrc, buildGradleTrg);
    }


    @Test
    public void scanDependencies() {
        GradleDependencyScanner gradleDependencyScanner = new GradleDependencyScanner();

        List<Dependency> dependencies = gradleDependencyScanner.scan(projectRoot);

        System.out.println(dependencies);
        Assert.assertEquals(6, dependencies.size());
        Assert.assertTrue(dependencies.contains(
            new Dependency("org.spockframework:spock-core",
                "1.1-groovy-2.4",
                "The Apache Software License, Version 2.0")));
    }
}
