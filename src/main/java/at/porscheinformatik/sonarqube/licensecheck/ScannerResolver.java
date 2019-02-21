package at.porscheinformatik.sonarqube.licensecheck;

import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.maven.MavenDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.npm.PackageJsonDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.service.JsonDependencyParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class ScannerResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScannerResolver.class);

    private ScannerResolver() {
    }

    static Scanner[] resolveScanners(File baseDir, Configuration configuration) {
        List<Scanner> scanners = new ArrayList<Scanner>();

        if (hasPomXml(baseDir)) {
            LOGGER.info("Found pom.xml in baseDir -> activating maven dependency scan.");
            scanners.add(new MavenDependencyScanner());
        }

        scanners.add(new PackageJsonDependencyScanner());
        scanners.add(new JsonDependencyParser(configuration));

        Scanner[] scannerArray = new Scanner[scanners.size()];
        return scanners.toArray(scannerArray);
    }

    private static boolean hasPomXml(File baseDir) {
        return new File(baseDir, "pom.xml").exists();
    }

}
