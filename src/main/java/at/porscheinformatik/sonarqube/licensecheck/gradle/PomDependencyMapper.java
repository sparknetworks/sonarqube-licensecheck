package at.porscheinformatik.sonarqube.licensecheck.gradle;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.gradle.license.LicenseMatcher;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;

import java.util.function.Predicate;

class PomDependencyMapper {

    private final LicenseMatcher licenseMatcher;

    PomDependencyMapper(LicenseMatcher licenseMatcher) {
        this.licenseMatcher = licenseMatcher;
    }

    Dependency toDependency(Model model) {
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
        return model.getLicenses().stream()
            .filter(licenseNameIsNotBlank())
            .filter(licenseMatcher.licenseHasMatchInLicenseMap())
            .map(License::getName)
            .findFirst()
            .orElse("");
    }

    private void inheritGroupOrVersionFromParent(Model pom) {
        if (pom.getGroupId() == null && pom.getParent().getGroupId() != null) {
            pom.setGroupId(pom.getParent().getGroupId());
        }
        if (pom.getVersion() == null && pom.getParent().getVersion() != null) {
            pom.setVersion(pom.getParent().getVersion());
        }
    }

    private Predicate<License> licenseNameIsNotBlank() {
        return license -> StringUtils.isNotBlank(license.getName());
    }
}
