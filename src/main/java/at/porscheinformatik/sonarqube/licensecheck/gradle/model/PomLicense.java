package at.porscheinformatik.sonarqube.licensecheck.gradle.model;

import java.util.Objects;

public class PomLicense {

    private String name;
    private String url;
    private String distribution;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PomLicense)) return false;
        PomLicense that = (PomLicense) o;
        return Objects.equals(name, that.name) &&
            Objects.equals(url, that.url) &&
            Objects.equals(distribution, that.distribution);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url, distribution);
    }

    @Override
    public String toString() {
        return "PomLicense{" +
            "name='" + name + '\'' +
            ", url='" + url + '\'' +
            ", distribution='" + distribution + '\'' +
            '}';
    }
}
