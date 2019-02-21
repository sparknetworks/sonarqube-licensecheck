package at.porscheinformatik.sonarqube.licensecheck.spdx;

import java.util.*;
import com.fasterxml.jackson.annotation.*;

public class LicensesWrapper {
    private String licenseListVersion;
    private List<SpdxLicense> licenses;
    private String releaseDate;

    @JsonProperty("licenseListVersion")
    public String getLicenseListVersion() { return licenseListVersion; }
    @JsonProperty("licenseListVersion")
    public void setLicenseListVersion(String value) { this.licenseListVersion = value; }

    @JsonProperty("licenses")
    public List<SpdxLicense> getLicenses() { return licenses; }
    @JsonProperty("licenses")
    public void setLicenses(List<SpdxLicense> value) { this.licenses = value; }

    @JsonProperty("releaseDate")
    public String getReleaseDate() { return releaseDate; }
    @JsonProperty("releaseDate")
    public void setReleaseDate(String value) { this.releaseDate = value; }
}
