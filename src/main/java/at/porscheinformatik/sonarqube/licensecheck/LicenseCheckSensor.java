package at.porscheinformatik.sonarqube.licensecheck;

import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputModule;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Settings;

import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.maven.MavenDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.npm.PackageJsonDependencyScanner;

public class LicenseCheckSensor implements Sensor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseCheckSensor.class);

    private final FileSystem fs;
    private final Settings settings;
    private final ValidateLicenses validateLicenses;
    private final Scanner[] scanners;

    public LicenseCheckSensor(FileSystem fs, Settings settings, ValidateLicenses validateLicenses,
        MavenLicenseService mavenLicenseService, MavenDependencyService mavenDependencyService)
    {
        this.fs = fs;
        this.settings = settings;
        this.validateLicenses = validateLicenses;
        this.scanners = ScannerResolver.resolveScanners(fs.baseDir(), mavenLicenseService, mavenDependencyService);
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
        if (settings.getBoolean(LicenseCheckPropertyKeys.ACTIVATION_KEY))
        {
            Set<Dependency> dependencies = new TreeSet<>();

            for (Scanner scanner : scanners)
            {
                dependencies.addAll(scanner.scan(fs.baseDir()));
            }

            ProjectDefinition project = ((DefaultInputModule) context.module()).definition().getParent();
            Set<Dependency> validatedDependencies = validateLicenses.validateLicenses(dependencies, context);
            Set<License> usedLicenses = validateLicenses.getUsedLicenses(validatedDependencies, project);

            saveDependencies(context, validatedDependencies);
            saveLicenses(context, usedLicenses);
        }
        else
        {
            LOGGER.info("Scanner is set to inactive. No scan possible.");
        }
    }

    private static void saveDependencies(SensorContext sensorContext, Set<Dependency> dependencies)
    {
        if (!dependencies.isEmpty())
        {
            sensorContext
                .newMeasure()
                .forMetric(LicenseCheckMetrics.INPUTDEPENDENCY)
                .withValue(Dependency.createString(dependencies))
                .on(sensorContext.module())
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
                .on(sensorContext.module())
                .save();
        }
    }
}
