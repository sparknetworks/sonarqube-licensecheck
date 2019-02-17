package at.porscheinformatik.sonarqube.licensecheck;

import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.internal.InternalDependenciesService;
import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseValidationResult;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.sensor.ProjectSensor;

import java.util.Set;
import java.util.TreeSet;

public class LicenseCheckSensor implements ProjectSensor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseCheckSensor.class);

    private final FileSystem fs;
    private final Configuration settings;
    private final ValidateLicenses validateLicenses;
    private final Scanner[] scanners;

    public LicenseCheckSensor(FileSystem fs, Configuration settings, ValidateLicenses validateLicenses,
                              MavenLicenseService mavenLicenseService, MavenDependencyService mavenDependencyService, InternalDependenciesService internalDependenciesService)
    {
        this.fs = fs;
        this.settings = settings;
        this.validateLicenses = validateLicenses;
        this.scanners = ScannerResolver.resolveScanners(fs.baseDir(), mavenLicenseService, mavenDependencyService, internalDependenciesService);
    }

    private static void saveDependencies(SensorContext sensorContext, Set<Dependency> dependencies)
    {
        if (!dependencies.isEmpty())
        {
            sensorContext
                .newMeasure()
                .forMetric(LicenseCheckMetrics.INPUTDEPENDENCY)
                .withValue(Dependency.createString(dependencies))
                .on(sensorContext.project())
                .save();
        }
    }

    private static void saveLicenses(SensorContext sensorContext, Set<License> licenses)
    {
        if (!licenses.isEmpty())
        {
            sensorContext
                .newMeasure()
                .forMetric(LicenseCheckMetrics.INPUTLICENSE)
                .withValue(License.createString(licenses))
                .on(sensorContext.project())
                .save();
        }
    }

    @Override
    public void describe(SensorDescriptor descriptor)
    {
        descriptor.name("License Check")
            .createIssuesForRuleRepository(LicenseCheckMetrics.LICENSE_CHECK_KEY);
    }

    @Override
    public void execute(SensorContext context)
    {
        if (settings.getBoolean(LicenseCheckPropertyKeys.ACTIVATION_KEY).orElse(false))
        {
            Set<Dependency> dependencies = new TreeSet<>();

            for (Scanner scanner : scanners)
            {
                dependencies.addAll(scanner.scan(fs.baseDir()));
            }
            LicenseValidationResult validatedDependencies = validateLicenses.validateLicenses(dependencies, context);
            LOGGER.debug("Validation result: {}", validatedDependencies);
            saveDependencies(context, validatedDependencies.getDependencies());
            saveLicenses(context, validatedDependencies.getLicenses());
        }
        else
        {
            LOGGER.info("Scanner is set to inactive. No scan possible.");
        }
    }
}
