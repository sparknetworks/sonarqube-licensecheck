package at.porscheinformatik.sonarqube.licensecheck.mavenlicense;

public class MavenLicense implements Comparable<MavenLicense> {
    private final String regex;
    private final String license;

    public MavenLicense(String regex, String license) {
        super();
        this.regex = regex;
        this.license = license;
    }

    public String getRegex() {
        return regex;
    }

    public String getLicense() {
        return license;
    }

    @Override
    public int compareTo(MavenLicense o) {
        if (o == null) {
            return 1;
        } else if (this.license.compareTo(o.license) == 0) {
            return this.regex.compareTo(o.regex);
        } else {
            return this.license.compareTo(o.license);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }

        MavenLicense mavenLicense = (MavenLicense) object;
        return mavenLicense.license.equals(this.license)
            && mavenLicense.regex.equals(this.regex);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((license == null) ? 0 : license.hashCode());
        result = (prime * result) + ((regex == null) ? 0 : regex.hashCode());
        return result;
    }

}
