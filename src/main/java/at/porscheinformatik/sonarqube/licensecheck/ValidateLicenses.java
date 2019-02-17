package at.porscheinformatik.sonarqube.licensecheck;

import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseValidationResult;
import at.porscheinformatik.sonarqube.licensecheck.maven.MavenDependencyScanner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.internal.DefaultIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scanner.ScannerSide;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
        if (!checkLicense(dependency.getLicense(), licenses, result)) {
            List<License> licensesContainingDependency = getLicenses(dependency.getLicense(), licenses);

            result.addAllLicenses(licensesContainingDependency);

            if (licensesContainingDependency.isEmpty()) {
                dependency.setStatus("Unknown");
                licenseNotFoundIssue(context, dependency);
            } else {
                StringBuilder notAllowedLicensees = new StringBuilder();

                for (License element : licensesContainingDependency) {
                    if (!element.getStatus()) {
                       notAllowedLicensees.append(element.getName()).append(" ");
                    }
                }
                dependency.setStatus("Forbidden");
                licenseNotAllowedIssue(context, dependency, notAllowedLicensees);
            }
        } else {
            dependency.setStatus("Allowed");
        }
    }

    private boolean checkLicense(String licenseString, List<License> licenses, LicenseValidationResult result) {
        if (licenseString.equals(MavenDependencyScanner.INTERNAL_LICENSE)) {
            // Short circuit internal licenses
            return true;
        }
        final List<License> licenseList = getLicenses(licenseString, licenses);
        LOGGER.info("Found liceses {} for license string {}", licenseList, licenseString);
        result.addAllLicenses(licenseList);
        if (licenseString.contains(" AND ")) {
            return licenseList.stream().allMatch(License::getStatus);
        }
        return licenseList.stream().anyMatch(License::getStatus);
    }

    private List<License> getLicenses(String licenseString, List<License> licenses) {
        List<String> licenseNames = Arrays.stream(licenseString.replace("(", "").replace(")", "").split(" (:?OR|AND) ")).map(String::trim).collect(Collectors.toList());
        return licenses
            .stream()
            .filter(l -> licenseNames.contains(l.getName()))
            .collect(Collectors.toList());
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
