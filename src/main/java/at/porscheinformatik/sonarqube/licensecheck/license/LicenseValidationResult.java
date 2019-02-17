package at.porscheinformatik.sonarqube.licensecheck.license;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class LicenseValidationResult {
    private Set<Dependency> dependencies;
    private Set<License> licenses;

    public LicenseValidationResult() {
        this.dependencies = new HashSet<>();
        this.licenses = new HashSet<>();
    }

    public void addAllLicenses(Collection<License> licenses) {
        this.licenses.addAll(licenses);
    }

    public void addLicense(License license) {
        this.licenses.add(license);
    }

    public void addAllDependencies(Collection<Dependency> dependencies) {
        this.dependencies.addAll(dependencies);
    }

    public void addDependency(Dependency dependency) {
        this.dependencies.add(dependency);
    }

    public Set<Dependency> getDependencies() {
        return new HashSet<>(dependencies);
    }

    public Set<License> getLicenses() {
        return new HashSet<>(licenses);
    }

    @Override
    public String toString() {
        return "LicenseValidationResult{ licenses=" + licenses.toString() + ", dependencies=" + dependencies.toString() + "}";
    }
}
