package at.porscheinformatik.sonarqube.licensecheck.spdx;

import java.util.*;

import at.porscheinformatik.sonarqube.licensecheck.license.License;
import com.fasterxml.jackson.annotation.*;

public class SpdxLicense {
    private String reference;
    private Boolean isDeprecatedLicenseID;
    private String detailsURL;
    private String referenceNumber;
    private String name;
    private String licenseID;
    private List<String> seeAlso;
    private Boolean isOSIApproved;
    private Boolean isFSFLibre;
    private String status;

    @JsonProperty("reference")
    public String getReference() { return reference; }
    @JsonProperty("reference")
    public void setReference(String value) { this.reference = value; }

    @JsonProperty("isDeprecatedLicenseId")
    public Boolean getIsDeprecatedLicenseID() { return isDeprecatedLicenseID; }
    @JsonProperty("isDeprecatedLicenseId")
    public void setIsDeprecatedLicenseID(Boolean value) { this.isDeprecatedLicenseID = value; }

    @JsonProperty("detailsUrl")
    public String getDetailsURL() { return detailsURL; }
    @JsonProperty("detailsUrl")
    public void setDetailsURL(String value) { this.detailsURL = value; }

    @JsonProperty("referenceNumber")
    public String getReferenceNumber() { return referenceNumber; }
    @JsonProperty("referenceNumber")
    public void setReferenceNumber(String value) { this.referenceNumber = value; }

    @JsonProperty("name")
    public String getName() { return name; }
    @JsonProperty("name")
    public void setName(String value) { this.name = value; }

    @JsonProperty("licenseId")
    public String getLicenseID() { return licenseID; }
    @JsonProperty("licenseId")
    public void setLicenseID(String value) { this.licenseID = value; }

    @JsonProperty("seeAlso")
    public List<String> getSeeAlso() { return seeAlso; }
    @JsonProperty("seeAlso")
    public void setSeeAlso(List<String> value) { this.seeAlso = value; }

    @JsonProperty("isOsiApproved")
    public Boolean getIsOSIApproved() { return isOSIApproved; }
    @JsonProperty("isOsiApproved")
    public void setIsOSIApproved(Boolean value) { this.isOSIApproved = value; }

    @JsonProperty("isFsfLibre")
    public Boolean getIsFSFLibre() { return isFSFLibre; }
    @JsonProperty("isFsfLibre")
    public void setIsFSFLibre(Boolean value) { this.isFSFLibre = value; }

    @JsonProperty("status")
    public String getStatus() { return status; }
    @JsonProperty("status")
    public void setStatus(String status) { this.status = status; }

    public License toLicense() {
        return new License(name, licenseID, false);
    }
}
