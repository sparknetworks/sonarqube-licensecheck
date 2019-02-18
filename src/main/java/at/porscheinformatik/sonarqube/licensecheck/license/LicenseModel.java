package at.porscheinformatik.sonarqube.licensecheck.license;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LicenseModel {
    private List<License> licenses = new ArrayList<>();
    private List<LicenseModel> models = new ArrayList<>();
    private Set<String> unmatched = new HashSet<>();
    private Operator operator;

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }


    public void addLicense(License license) {
        this.licenses.add(license);
    }

    public void addModel(LicenseModel model) {
        this.models.add(model);
    }

    public boolean isAllowed(List<License> licenseList) {
        if (operator == null || operator == Operator.OR) {
            return licenseList.stream().filter(licenses::contains).anyMatch(License::getStatus) || models.stream().anyMatch(it -> it.isAllowed(licenseList));
        } else {
            return licenseList.stream().filter(licenses::contains).allMatch(License::getStatus) && (models.isEmpty() || models.stream().anyMatch(it -> it.isAllowed(licenseList)));
        }
    }

    public boolean hasUnmatched() {
        return !unmatched.isEmpty();
    }

    public boolean isEmpty() {
        return getUsedLicenses().isEmpty();
    }

    public List<License> getUsedLicenses() {
        List<License> returningLicenses = new ArrayList<>();
        returningLicenses.addAll(this.licenses);
        returningLicenses.addAll(models.stream()
            .flatMap(it -> it.getUsedLicenses().stream())
            .collect(Collectors.toList()));
        return returningLicenses;
    }

    @Override
    public String toString() {
        return "LicenseModel{" +
            "licenses=" + licenses +
            ", models=" + models +
            ", operator=" + operator +
            '}';
    }

    public void addUnmatched(String license) {
        this.unmatched.add(license);
    }

    public Set<String> getUnmatched() {
        return unmatched;
    }
}
