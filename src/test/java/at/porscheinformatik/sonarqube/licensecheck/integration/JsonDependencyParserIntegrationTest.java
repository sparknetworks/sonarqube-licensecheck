package at.porscheinformatik.sonarqube.licensecheck.integration;

import at.porscheinformatik.sonarqube.licensecheck.ProjectResolver;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.model.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.service.JsonDependencyParser;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static at.porscheinformatik.sonarqube.licensecheck.sonarqube.SonarqubeConfigurationHelper.mockConfiguration;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class JsonDependencyParserIntegrationTest {


    private static File projectRoot;

    @Before
    public void setup() throws IOException {
        final File parentFolder = new File(this.getClass().getClassLoader().getResource("license_check_plugin/licenseReport.json").getPath()).getParentFile();
        projectRoot = ProjectResolver.prepareProject(() -> parentFolder, (file) -> {
        });
    }

    @Test
    public void shouldParseDependenciesFromLicenseReportFile() {
        final Configuration configuration = mock(Configuration.class);
        mockConfiguration(configuration);
        Scanner scanner = new JsonDependencyParser(configuration);
        final List<Dependency> dependencies = scanner.scan(projectRoot);

        assertThat(dependencies, notNullValue());
        assertThat(dependencies, hasSize(183));
        assertThat(dependencies.stream().map(Dependency::getLicense).filter(Objects::nonNull).collect(Collectors.toList()), hasSize(greaterThan(5)));
        assertThat(dependencies.stream().anyMatch(it -> !it.getLicenses().isEmpty()), is(true));
    }

}
