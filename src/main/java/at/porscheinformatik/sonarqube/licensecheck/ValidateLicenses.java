package at.porscheinformatik.sonarqube.licensecheck;

import at.porscheinformatik.sonarqube.licensecheck.license.*;
import at.porscheinformatik.sonarqube.licensecheck.maven.MavenDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.spdx.LicenseProvider;
import at.porscheinformatik.sonarqube.licensecheck.spdx.SpdxLicense;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.internal.DefaultIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scanner.ScannerSide;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ScannerSide
public class ValidateLicenses {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateLicenses.class);
    private final LicenseService licenseService;

    public ValidateLicenses(LicenseService licenseService) {
        super();
        this.licenseService = licenseService;
    }

    public LicenseValidationResult validateLicenses(Set<Dependency> dependencies, SensorContext context) {
        LicenseValidationResult result = new LicenseValidationResult();
        result.addAllDependencies(dependencies);
        for (Dependency dependency : dependencies) {
            if (StringUtils.isBlank(dependency.getLicense())) {
                licenseNotFoundIssue(context, dependency);
            } else {
                checkForLicenses(context, dependency, result);
            }
        }
        return result;
    }

    public Set<License> getUsedLicenses(Set<Dependency> dependencies) {
        Set<License> usedLicenseList = new TreeSet<>();
        List<License> licenses = licenseService.getLicenses();

        for (Dependency dependency : dependencies) {
            for (License license : licenses) {
                if (license.getIdentifier().equals(dependency.getLicense())) {
                    usedLicenseList.add(license);
                }
            }
        }
        return usedLicenseList;
    }

    private void checkForLicenses(SensorContext context, Dependency dependency, LicenseValidationResult result) {
        List<License> licenses = licenseService.getLicenses();
        if (MavenDependencyScanner.INTERNAL_LICENSE.equals(dependency.getLicense())) {
            dependency.setStatus("Allowed");
            return;
        }
        final LicenseModel licenseModel = constructModel(dependency.getLicense());
        if (licenseModel.hasUnmatched()) {
            dependency.setStatus("Unknown");
            licenseNotFoundIssue(context, dependency);
        } else if (!licenseModel.isAllowed(licenses)) {
            List<License> licensesContainingDependency = licenseModel.getUsedLicenses();
            result.addAllLicenses(licensesContainingDependency);
            StringBuilder notAllowedLicensees = new StringBuilder();
            for (License element : licensesContainingDependency) {
                if (!element.getStatus()) {
                    notAllowedLicensees.append(element.getName()).append(" ");
                }
            }
            dependency.setStatus("Forbidden");
            licenseNotAllowedIssue(context, dependency, notAllowedLicensees);
        } else {
            dependency.setStatus("Allowed");
        }
    }


    private LicenseModel constructModel(String licenseString) {
        LicenseModel model = new LicenseModel();

        String current = licenseString;
        if (current.startsWith("(")) {
            current = current.substring(1);
        }

        if (current.endsWith(")")) {
            current = current.substring(0, current.length() - 1);
        }

        Pattern pattern = Pattern.compile("(\\([^(]+\\))");
        final Matcher matcher = pattern.matcher(current);

        while (matcher.find()) {
            model.addModel(constructModel(matcher.group(0)));
        }

        current = matcher.replaceAll("").trim();

        if (current.contains("AND")) {
            model.setOperator(Operator.AND);
            Arrays.stream(current.split("AND")).map(String::trim).forEach(string -> addLicense(model, string));
        } else if (current.contains("OR")) {
            model.setOperator(Operator.OR);
            Arrays.stream(current.split("OR")).map(String::trim).forEach(string -> addLicense(model, string));
        } else {
            addLicense(model, current);
        }
        return model;
    }

    private void addLicense(LicenseModel model, String licenseString) {
        final Optional<License> optionalLicense = LicenseProvider.getByNameOrIdentifier(licenseString.trim()).map(SpdxLicense::toLicense);
        if (optionalLicense.isPresent()) {
            optionalLicense.ifPresent(model::addLicense);
        } else {
            model.addUnmatched(licenseString.trim());
        }
    }

    private void licenseNotAllowedIssue(SensorContext context, Dependency dependency, StringBuilder notAllowedLicense) {
        LOGGER.info("Dependency  {}  uses a disallowed license {}", dependency, notAllowedLicense);

        NewIssue issue = context
            .newIssue()
            .forRule(RuleKey.of(LicenseCheckMetrics.LICENSE_CHECK_KEY,
                LicenseCheckMetrics.LICENSE_CHECK_NOT_ALLOWED_LICENSE_KEY))
            .at(new DefaultIssueLocation().on(context.project()).message(
                "Dependency " + dependency.getName() + " uses a not allowed license " + dependency.getLicense()));
        issue.save();
    }

    private static void licenseNotFoundIssue(SensorContext context, Dependency dependency) {
        LOGGER.info("No License found for Dependency {}", dependency.getName());

        NewIssue issue = context
            .newIssue()
            .forRule(RuleKey.of(LicenseCheckMetrics.LICENSE_CHECK_KEY,
                LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY))
            .at(new DefaultIssueLocation()
                .on(context.project())
                .message("No License found for Dependency: " + dependency.getName()));
        issue.save();
    }
}
