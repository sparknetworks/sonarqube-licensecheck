package at.porscheinformatik.sonarqube.licensecheck.spdx;

import at.porscheinformatik.sonarqube.licensecheck.model.LicenseDefinition;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

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
    public Boolean isDeprecatedLicenseID() { return isDeprecatedLicenseID; }
    @JsonProperty("isDeprecatedLicenseId")
    public void setDeprecatedLicenseID(Boolean value) { this.isDeprecatedLicenseID = value; }

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
    public Boolean isOSIApproved() { return isOSIApproved; }
    @JsonProperty("isOsiApproved")
    public void setOSIApproved(Boolean value) { this.isOSIApproved = value; }

    @JsonProperty("isFsfLibre")
    public Boolean isFSFLibre() { return isFSFLibre; }
    @JsonProperty("isFsfLibre")
    public void setFSFLibre(Boolean value) { this.isFSFLibre = value; }

    @JsonProperty("status")
    public String getStatus() { return status; }
    @JsonProperty("status")
    public void setStatus(String status) { this.status = status; }

    public LicenseDefinition toLicense() {
        return new LicenseDefinition(name, licenseID, false);
    }
}
