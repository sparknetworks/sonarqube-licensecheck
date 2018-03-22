package at.porscheinformatik.sonarqube.licensecheck.gradle;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GradleDependencyScanner implements Scanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(GradleDependencyScanner.class);

    private final MavenLicenseService mavenLicenseService;
    private final MavenDependencyService mavenDependencyService;

    private File projectRoot;

    public GradleDependencyScanner(MavenLicenseService mavenLicenseService, MavenDependencyService mavenDependencyService) {
        this.mavenLicenseService = mavenLicenseService;
        this.mavenDependencyService = mavenDependencyService;
    }

    @Override
    public List<Dependency> scan(File moduleDir) {
        this.projectRoot = moduleDir;

        try {
            return resolveDependenciesWithLicenses();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Dependency> resolveDependenciesWithLicenses() throws Exception {
        GradlePomResolver gradlePomResolver = new GradlePomResolver(projectRoot);

        List<Model> poms = gradlePomResolver.resolvePomsOfAllDependencies();
        List<Dependency> dependencies = pomsToDependencies(poms);

        return dependencies.stream()
            .map(this::matchLicenseByMavenDependency)
            .map(this::mapLicenseByMatcher)
            .collect(Collectors.toList());
    }

    private List<Dependency> pomsToDependencies(List<Model> poms) {
        return poms.stream()
            .map(this::pomToDependency)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Dependency pomToDependency(Model model) {

        inheritGroupOrVersionFromParent(model);

        if (StringUtils.isBlank(model.getGroupId())
            || StringUtils.isBlank(model.getArtifactId())
            || StringUtils.isBlank(model.getVersion())) {
            return null;
        }

        return new Dependency(
            model.getGroupId() + ":" + model.getArtifactId(),
            model.getVersion(),
            selectMatchingLicenseFromLicenses(model));
    }

    private String selectMatchingLicenseFromLicenses(Model model) {
        String license = "";

        if (model.getLicenses() != null && model.getLicenses().size() > 0) {
            license = model.getLicenses().get(0).getName();
        }

        if (mavenLicenseService != null
            && mavenLicenseService.getLicenseMap() != null) {
            license = model.getLicenses().stream()
                .filter(licenseNameIsNotBlank())
                .filter(licenseHasMatchInLicenseMap())
                .map(License::getName)
                .findFirst()
                .orElse("");
        }
        return license;
    }

    private Predicate<License> licenseNameIsNotBlank() {
        return license -> StringUtils.isNotBlank(license.getName());
    }

    private Predicate<License> licenseHasMatchInLicenseMap() {
        return license -> mavenLicenseService.getLicenseMap().entrySet().stream()
            .map(entry -> entry.getKey().matcher(license.getName()).matches())
            .findFirst()
            .orElse(false);
    }

    private void inheritGroupOrVersionFromParent(Model pom) {
        if (pom.getGroupId() == null && pom.getParent().getGroupId() != null) {
            pom.setGroupId(pom.getParent().getGroupId());
        }
        if (pom.getVersion() == null && pom.getParent().getVersion() != null) {
            pom.setVersion(pom.getParent().getVersion());
        }
    }

    // todo: reuse methods from MavenDependencyScanner?
    private Dependency matchLicenseByMavenDependency(Dependency dependency) {
        if (StringUtils.isBlank(dependency.getLicense())
            && mavenDependencyService != null
            && mavenDependencyService.getMavenDependencies() != null) {
            for (MavenDependency allowedDependency : mavenDependencyService.getMavenDependencies()) {
                String matchString = allowedDependency.getKey();
                if (dependency.getName().matches(matchString)) {
                    dependency.setLicense(allowedDependency.getLicense());
                }
            }
        }
        return dependency;
    }

    private Dependency mapLicenseByMatcher(Dependency dependency) {
        String licenseName = dependency.getLicense();
        if (mavenLicenseService != null && mavenLicenseService.getLicenseMap() != null) {
            for (Map.Entry<Pattern, String> entry : mavenLicenseService.getLicenseMap().entrySet()) {
                if (entry.getKey().matcher(licenseName).matches()) {
                    dependency.setLicense(entry.getValue());
                    return dependency;
                }
            }
        }
        LOGGER.debug("Could not match license: " + licenseName);
        return dependency;
    }
}
