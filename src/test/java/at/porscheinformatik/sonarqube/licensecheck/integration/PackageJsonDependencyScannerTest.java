package at.porscheinformatik.sonarqube.licensecheck.integration;

import at.porscheinformatik.sonarqube.licensecheck.ProjectResolver;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.model.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.npm.PackageJsonDependencyScanner;
import org.junit.Test;
import org.zeroturnaround.zip.Zips;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

public class PackageJsonDependencyScannerTest {
    private Consumer<File> unpackModules = (file) -> {
        final File nodeModulesZip = file.toPath().resolve("node_modules.zip").toFile();
        if (nodeModulesZip.canRead()) {
            Zips.get(nodeModulesZip).destination(file).unpack().process();
        }
    };

    @Test
    public void shouldOnlyRetrieveRuntimeDependencies() throws IOException {
        final File parentFolder = new File(this.getClass().getClassLoader().getResource("npm/package.json").getPath()).getParentFile();
        final File projectRoot = ProjectResolver.prepareProject(() -> parentFolder, unpackModules);

        Scanner scanner = new PackageJsonDependencyScanner();

        List<Dependency> dependencies = scanner.scan(projectRoot);

        assertThat(dependencies, hasItem(new Dependency("angular", "1.5.0", "MIT")));
        assertThat(dependencies, not(hasItem(new Dependency("gulp", "3.9.1", "MIT"))));
    }


    @Test
    public void shouldRetrieveDependenciesOfDependencies() throws IOException {

        final File parentFolder = new File(this.getClass().getClassLoader().getResource("npm-large/package.json").getPath()).getParentFile();

        final File projectRoot = ProjectResolver.prepareProject(() -> parentFolder, unpackModules);
        Scanner scanner = new PackageJsonDependencyScanner();

        List<Dependency> dependencies = scanner.scan(projectRoot);

        assertThat(dependencies, hasItem(new Dependency("angular", "1.7.7", "MIT")));
        assertThat(dependencies, hasItem(new Dependency("messageformat-parser", "3.0.0", "MIT")));
    }

    @Test
    public void shouldNotOverflowStackOnCircularDependencies() throws IOException {

        final File parentFolder = new File(this.getClass().getClassLoader().getResource("npm-circular/package.json").getPath()).getParentFile();
        final File projectRoot = ProjectResolver.prepareProject(() -> parentFolder, unpackModules);
        Scanner scanner = new PackageJsonDependencyScanner();

        List<Dependency> dependencies = scanner.scan(projectRoot);

        assertThat(dependencies, not(empty()));
    }

}
