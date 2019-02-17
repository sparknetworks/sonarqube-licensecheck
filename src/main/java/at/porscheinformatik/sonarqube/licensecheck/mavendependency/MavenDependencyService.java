package at.porscheinformatik.sonarqube.licensecheck.mavendependency;

import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.*;

@ServerSide
@ScannerSide
public class MavenDependencyService {
    private final Configuration configuration;

    public MavenDependencyService(Configuration configuration) {
        super();
        this.configuration = configuration;
    }

    public List<MavenDependency> getMavenDependencies() {
        return Arrays.stream(configuration.getStringArray(MAVEN_REGEX)).map(it -> {
            final Optional<String> regex = configuration.get(MAVEN_REGEX + "." + it + "." + NAME_MATCHES);
            final Optional<String> license = configuration.get(MAVEN_REGEX + "." + it + "." + LICENSE);
            if (regex.isPresent() && license.isPresent()) {
                return new MavenDependency(regex.get(), license.get());
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
