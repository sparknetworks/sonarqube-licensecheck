package at.porscheinformatik.sonarqube.licensecheck.gradle;

import at.porscheinformatik.sonarqube.licensecheck.gradle.model.PomLicense;
import at.porscheinformatik.sonarqube.licensecheck.gradle.model.PomProject;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class GradlePomResolverTest {


    private static File projectRoot;

    // todo: mock GradleInvoker

    @Before
    public void setup() throws IOException {
        projectRoot = new File("build/testProject");
        projectRoot.mkdirs();

        File buildGradleSrc = new File(this.getClass().getClassLoader().getResource("gradle/build.gradle").getFile());
        File buildGradleTrg = new File(projectRoot, "build.gradle");
        FileUtils.copyFile(buildGradleSrc, buildGradleTrg);
    }

    @Test
    public void resolvePoms() throws Exception {
        GradlePomResolver gradlePomResolver = new GradlePomResolver(projectRoot);

        List<PomProject> poms = gradlePomResolver.resolvePoms();

        PomProject pomProject = new PomProject();
        pomProject.setArtifactId("spock-core");
        pomProject.setGroupId("org.spockframework");
        pomProject.setVersion("1.1-groovy-2.4");
        PomLicense pomLicense = new PomLicense();
        pomLicense.setName("The Apache Software License, Version 2.0");
        pomLicense.setUrl("http://www.apache.org/licenses/LICENSE-2.0.txt");
        pomLicense.setDistribution("repo");
        pomProject.setLicenses(Collections.singletonList(pomLicense));

        Assert.assertTrue(poms.contains(pomProject));
    }
}
