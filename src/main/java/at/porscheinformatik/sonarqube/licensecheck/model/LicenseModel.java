package at.porscheinformatik.sonarqube.licensecheck.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LicenseModel {
    private List<LicenseDefinition> licenses = new ArrayList<>();
    private List<LicenseModel> models = new ArrayList<>();
    private Set<String> unmatched = new HashSet<>();
    private Operator operator;

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }


    public void addLicense(LicenseDefinition license) {
        this.licenses.add(license);
    }

    public void addModel(LicenseModel model) {
        this.models.add(model);
    }

    public boolean isAllowed(List<LicenseDefinition> licenseList) {
        if (operator == null || operator == Operator.OR) {
            return licenseList.stream().filter(licenses::contains).anyMatch(LicenseDefinition::getStatus) || models.stream().anyMatch(it -> it.isAllowed(licenseList));
        } else {
            return licenseList.stream().filter(licenses::contains).allMatch(LicenseDefinition::getStatus) && (models.isEmpty() || models.stream().anyMatch(it -> it.isAllowed(licenseList)));
        }
    }

    public boolean hasUnmatched() {
        return !unmatched.isEmpty();
    }

    public boolean isEmpty() {
        return getUsedLicenses().isEmpty();
    }

    public List<LicenseDefinition> getUsedLicenses() {
        List<LicenseDefinition> returningLicenses = new ArrayList<>();
        returningLicenses.addAll(this.licenses);
        returningLicenses.addAll(models.stream()
            .flatMap(it -> it.getUsedLicenses().stream())
            .collect(Collectors.toList()));
        return returningLicenses;
    }

    @Override
    public String toString() {
        return "LicenseModel{" + generateSpdxLicenseInfo() + '}';
    }

    public String generateSpdxLicenseInfo() {
        final List<String> allLicenses = this.licenses.stream().map(LicenseDefinition::getIdentifier).collect(Collectors.toList());
        allLicenses.addAll(this.unmatched.stream().map(it -> "u:" + it).collect(Collectors.toList()));
        if (allLicenses.size() == 1 && models.isEmpty()) {
            return allLicenses.get(0);
        }
        StringBuilder builder = new StringBuilder();
        if (operator != null) {
            builder.append("(").append(String.join(operator.delimiter(), allLicenses)).append(")");
            if (!this.models.isEmpty()) {
                builder.append("(").append(this.models.stream().map(LicenseModel::generateSpdxLicenseInfo).collect(Collectors.joining(operator.delimiter()))).append(")");
            }
        }
        return builder.toString();
    }

    public void addUnmatched(String license) {
        this.unmatched.add(license);
    }

    public Set<String> getUnmatched() {
        return unmatched;
    }
}
