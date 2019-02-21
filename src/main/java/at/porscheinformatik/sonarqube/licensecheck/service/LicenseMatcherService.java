package at.porscheinformatik.sonarqube.licensecheck.service;

import at.porscheinformatik.sonarqube.licensecheck.model.Dependency;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ServerSide
@ScannerSide
public class LicenseMatcherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseMatcherService.class);
    private final LicenseMappingService licenseMappingService;
    private final ArtifactDependencyService artifactDependencyService;
    private final InternalDependenciesService internalDependenciesService;


    public LicenseMatcherService(LicenseMappingService licenseMappingService, ArtifactDependencyService artifactDependencyService, InternalDependenciesService internalDependenciesService) {
        super();
        this.licenseMappingService = licenseMappingService;
        this.artifactDependencyService = artifactDependencyService;
        this.internalDependenciesService = internalDependenciesService;
    }

    public void matchLicenses(Collection<Dependency> dependencies) {
        dependencies.forEach(this::matchLicense);
    }

    public void matchLicense(Dependency dependency) {
        if (StringUtils.isBlank(dependency.getLicense())) {
            final String dependencyName = dependency.getName();
            artifactDependencyService.matchByPackage(dependencyName).ifPresent(dependency::setLicense);
            internalDependenciesService.matchByPackage(dependencyName).ifPresent(dependency::setLicense);
        }
        if (StringUtils.isBlank(dependency.getLicense())) {
            LOGGER.info("Dependency '{}' has no license set.", dependency.getName());
            return;
        }
        if (!dependency.getLicenses().isEmpty()) {
            final Set<String> postProcessedLicenses = dependency.getLicenses()
                .stream()
                .map(licenseMappingService::matchLicense)
                .filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toSet());
            dependency.setLicenses(postProcessedLicenses);
        } else {
            licenseMappingService.matchLicense(dependency.getLicense()).ifPresent(dependency::setLicense);
        }
    }

}
