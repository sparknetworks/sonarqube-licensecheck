package at.porscheinformatik.sonarqube.licensecheck.gradle.model;

import java.util.Objects;

public class ParentProject {
    @Override
    public String toString() {
        return "ParentProject{" +
            "groupId='" + groupId + '\'' +
            ", version='" + version + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParentProject)) return false;
        ParentProject that = (ParentProject) o;
        return Objects.equals(groupId, that.groupId) &&
            Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, version);
    }

    private String groupId;
    private String version;

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
}
