package at.porscheinformatik.sonarqube.licensecheck;

import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.model.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.model.LicenseDefinition;
import at.porscheinformatik.sonarqube.licensecheck.model.LicenseValidationResult;
import at.porscheinformatik.sonarqube.licensecheck.service.LicenseMatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.sensor.ProjectSensor;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class LicenseCheckSensor implements ProjectSensor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseCheckSensor.class);

    private final FileSystem fs;
    private final Configuration configuration;
    private final ValidateLicenses validateLicenses;
    private final LicenseMatcherService licenseMatcherService;
    private final Scanner[] scanners;

    public LicenseCheckSensor(FileSystem fs, Configuration configuration, ValidateLicenses validateLicenses,
                              LicenseMatcherService licenseMatcherService) {
        this.fs = fs;
        this.configuration = configuration;
        this.validateLicenses = validateLicenses;
        this.licenseMatcherService = licenseMatcherService;
        this.scanners = ScannerResolver.resolveScanners(fs.baseDir(), configuration);
    }

    private static void saveDependencies(SensorContext sensorContext, Set<Dependency> dependencies) {
        if (!dependencies.isEmpty()) {
            sensorContext
                .newMeasure()
                .forMetric(LicenseCheckMetrics.INPUTDEPENDENCY)
                .withValue(Dependency.createString(dependencies))
                .on(sensorContext.project())
                .save();
        }
    }

    private static void saveLicenses(SensorContext sensorContext, Set<LicenseDefinition> licenses) {
        if (!licenses.isEmpty()) {
            sensorContext
                .newMeasure()
                .forMetric(LicenseCheckMetrics.INPUTLICENSE)
                .withValue(LicenseDefinition.createString(licenses))
                .on(sensorContext.project())
                .save();
        }
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.name("License Check")
            .createIssuesForRuleRepository(LicenseCheckMetrics.LICENSE_CHECK_KEY);
    }

    @Override
    public void execute(SensorContext context) {
        if (configuration.getBoolean(LicenseCheckPropertyKeys.ACTIVATION_KEY).orElse(false)) {
            Set<Dependency> dependencies = new TreeSet<>();

            for (Scanner scanner : scanners) {
                final List<Dependency> scan = scanner.scan(fs.baseDir());
                if (scan != null) {
                    dependencies.addAll(scan);
                }
            }
            licenseMatcherService.matchLicenses(dependencies);
            LicenseValidationResult validatedDependencies = validateLicenses.validateLicenses(dependencies, context);
            LOGGER.debug("Validation result: {}", validatedDependencies);
            saveDependencies(context, validatedDependencies.getDependencies());
            saveLicenses(context, validatedDependencies.getLicenses());
        } else {
            LOGGER.info("Scanner is set to inactive. No scan possible.");
        }
    }
}
