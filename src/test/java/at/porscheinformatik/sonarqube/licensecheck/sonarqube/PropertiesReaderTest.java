package at.porscheinformatik.sonarqube.licensecheck.sonarqube;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Configuration;

import java.util.Map;
import java.util.stream.Collectors;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.NAME;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.NAME_MATCHES;
import static at.porscheinformatik.sonarqube.licensecheck.sonarqube.SonarqubeConfigurationHelper.map;
import static at.porscheinformatik.sonarqube.licensecheck.sonarqube.SonarqubeConfigurationHelper.mockConfiguration;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class PropertiesReaderTest {

    private Configuration configuration;

    @Before
    public void setup() {
        configuration = mock(Configuration.class);
    }


    @Test
    public void shouldCorrectlyRetrieveListFromSingleValueProperty() {
        final String parentKey = "some.key";
        mockConfiguration(configuration, parentKey, map(NAME, "name1"), map(NAME, "name2"));
        assertThat(PropertiesReader.retrieveStringList(configuration, parentKey, NAME), containsInAnyOrder("name1", "name2"));
    }

    @Test
    public void shouldRetrieveStreamOfMapItemsWithElements() {
        final String parentKey = "another.key";
        final Map<String, String> first = map(NAME, "name1", NAME_MATCHES, "*234*");
        final Map<String, String> second = map(NAME, "name2", NAME_MATCHES, "*345*");
        mockConfiguration(configuration, parentKey, first, second);
        assertThat(PropertiesReader.retrieveMapStream(configuration, parentKey, NAME, NAME_MATCHES).collect(Collectors.toList()), containsInAnyOrder(first, second));
    }


}
