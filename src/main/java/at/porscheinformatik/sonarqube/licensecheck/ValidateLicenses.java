package at.porscheinformatik.sonarqube.licensecheck;

import at.porscheinformatik.sonarqube.licensecheck.maven.MavenDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.model.*;
import at.porscheinformatik.sonarqube.licensecheck.service.LicenseDefinitionService;
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
import java.util.stream.Collectors;

@ScannerSide
public class ValidateLicenses {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateLicenses.class);
    private final LicenseDefinitionService licenseDefinitionService;

    public ValidateLicenses(LicenseDefinitionService licenseDefinitionService) {
        super();
        this.licenseDefinitionService = licenseDefinitionService;
    }

    public LicenseValidationResult validateLicenses(Set<Dependency> dependencies, SensorContext context) {
        LicenseValidationResult result = new LicenseValidationResult();
        result.addAllDependencies(dependencies);
        for (Dependency dependency : dependencies) {
            if (StringUtils.isBlank(dependency.getLicense())) {
                licenseNotFoundIssue(context, dependency, null);
            } else {
                checkForLicenses(context, dependency, result);
            }
        }
        return result;
    }

    public Set<LicenseDefinition> getUsedLicenses(Set<Dependency> dependencies) {
        Set<LicenseDefinition> usedLicenseList = new TreeSet<>();
        List<LicenseDefinition> licenses = licenseDefinitionService.getLicenses();

        for (Dependency dependency : dependencies) {
            for (LicenseDefinition license : licenses) {
                if (license.getIdentifier().equals(dependency.getLicense())) {
                    usedLicenseList.add(license);
                }
            }
        }
        return usedLicenseList;
    }

    private void checkForLicenses(SensorContext context, Dependency dependency, LicenseValidationResult result) {
        List<LicenseDefinition> licenses = licenseDefinitionService.getLicenses();
        if (MavenDependencyScanner.INTERNAL_LICENSE.equals(dependency.getLicense())) {
            dependency.setStatus("Allowed");
            return;
        }
        final LicenseModel licenseModel;
        // If we do not have correct information about the operator between the licenses we should err on the side of caution and as such we define the operator to be an AND instead of an OR
        if (dependency.getLicenses().size() > 1) {
            licenseModel = modelFromListWithNoOperator(dependency);
        } else {
            licenseModel = constructModel(dependency.getLicense());
        }
        final List<LicenseDefinition> usedLicenses = licenseModel.getUsedLicenses();
        if (licenseModel.hasUnmatched()) {
            dependency.setStatus("Unknown");
            licenseNotFoundIssue(context, dependency, licenseModel.getUnmatched());
        } else if (!licenseModel.isAllowed(licenses)) {
            StringBuilder notAllowedLicensees = new StringBuilder();
            for (LicenseDefinition element : usedLicenses) {
                if (!element.getStatus()) {
                    notAllowedLicensees.append(element.getName()).append(" ");
                }
            }
            dependency.setStatus("Forbidden");
            LOGGER.debug("Disallowed license model: {}", licenseModel);
            licenseNotAllowedIssue(context, dependency, notAllowedLicensees);
        } else {
            dependency.setStatus("Allowed");
        }
        dependency.setLicense(licenseModel.generateSpdxLicenseInfo());
        result.addAllLicenses(licenses.stream().filter(usedLicenses::contains).collect(Collectors.toList()));
    }

    private LicenseModel modelFromListWithNoOperator(Dependency dependency) {
        LicenseModel licenseModel;
        licenseModel = new LicenseModel();
        licenseModel.setOperator(Operator.AND);
        Set<String> licenseStrings = dependency.getLicenses();
        Set<String> definitions = licenseStrings.stream()
            .map(LicenseProvider::getByNameOrIdentifier)
            .filter(Optional::isPresent)
            .map(Optional::get).map(it -> it.getName() + it.getLicenseID())
            .collect(Collectors.toSet());
        if (definitions.size() != licenseStrings.size()) {
            licenseStrings.stream().filter(it -> definitions.stream().noneMatch(def -> def.contains(it))).forEach(licenseModel::addUnmatched);
        }
        return licenseModel;
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
        final Optional<LicenseDefinition> optionalLicense = LicenseProvider.getByNameOrIdentifier(licenseString.trim()).map(SpdxLicense::toLicense);
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

    private static void licenseNotFoundIssue(SensorContext context, Dependency dependency, Set<String> unmatched) {
        LOGGER.info("No License found for Dependency {}", dependency.getName());

        NewIssue issue = context
            .newIssue()
            .forRule(RuleKey.of(LicenseCheckMetrics.LICENSE_CHECK_KEY,
                LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY))
            .at(new DefaultIssueLocation()
                .on(context.project())
                .message("No License found for Dependency: " + dependency.getName() + ", Unknown licenses: " + unmatched));
        issue.save();
    }
}
