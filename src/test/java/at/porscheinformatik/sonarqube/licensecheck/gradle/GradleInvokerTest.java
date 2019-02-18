package at.porscheinformatik.sonarqube.licensecheck.gradle;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static at.porscheinformatik.sonarqube.licensecheck.gradle.GradleProjectResolver.prepareGradleProject;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class GradleInvokerTest {

    private static File root;

    @Before
    public void setup() throws IOException {
        root = prepareGradleProject();
    }

    @Test
    public void invokeTasks() throws Exception {
        GradleInvoker gradleInvoker = new GradleInvoker(root.getAbsolutePath());

        assertThat(gradleInvoker.invoke("tasks"), containsString("build"));
    }

    @Test
    public void invokeDependenciesBare() throws Exception {
        GradleInvoker gradleInvoker = new GradleInvoker(root.getAbsolutePath());

        assertThat(gradleInvoker.invoke("dependencies"), containsString("groovy"));
    }
}
