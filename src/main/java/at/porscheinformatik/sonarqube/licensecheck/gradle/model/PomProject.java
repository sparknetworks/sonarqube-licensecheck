package at.porscheinformatik.sonarqube.licensecheck.gradle.model;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;

import java.util.List;
import java.util.Objects;

public class PomProject {
    private List<PomLicense> licenses;
    private String artifactId;
    private String groupId;
    private String version;

    public Dependency toDependency() {
        String license = null;
        if (this.licenses != null && this.licenses.size() > 0) {
            license = this.licenses.get(0).getName();
        }
        return new Dependency(
            this.groupId + ":" + this.artifactId,
            this.version,
            license);
    }

    public List<PomLicense> getLicenses() {
        return licenses;
    }

    public void setLicenses(List<PomLicense> licenses) {
        this.licenses = licenses;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "PomProject{" +
            "licenses=" + licenses +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PomProject)) return false;
        PomProject that = (PomProject) o;
        return Objects.equals(licenses, that.licenses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(licenses);
    }

}
