package at.porscheinformatik.sonarqube.licensecheck.sonarqube;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.NAME;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.NAME_MATCHES;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PropertiesScannerTest {

    private Configuration configuration;

    @Before
    public void setup() {
        configuration = mock(Configuration.class);
    }


    @Test
    public void shouldCorrectlyRetrieveListFromSingleValueProperty() {
        final String parentKey = "some.key";
        mockConfiguration(parentKey, map(NAME, "name1"), map(NAME, "name2"));
        assertThat(PropertiesScanner.retrieveStringList(configuration, parentKey, NAME), containsInAnyOrder("name1", "name2"));
    }

    @Test
    public void shouldRetrieveStreamOfMapItemsWithElements() {
        final String parentKey = "another.key";
        final Map<String, String> first = map(NAME, "name1", NAME_MATCHES, "*234*");
        final Map<String, String> second = map(NAME, "name2", NAME_MATCHES, "*345*");
        mockConfiguration(parentKey, first, second);
        assertThat(PropertiesScanner.retrieveMapStream(configuration, parentKey, NAME, NAME_MATCHES).collect(Collectors.toList()), containsInAnyOrder(first, second));
    }


    private void mockConfiguration(String parentKey, Map<String, String>... entries) {
        String[] keys = new String[entries.length];
        for (int i = 0; i < entries.length; i++) {
            Map<String, String> entry = entries[i];
            final String index = String.valueOf(i);
            entry.forEach((key, value) -> when(this.configuration.get(PropertiesScanner.constructKey(parentKey, index, key))).thenReturn(Optional.of(value)));
            keys[i] = index;
        }
        when(this.configuration.getStringArray(parentKey)).thenReturn(keys);
    }

    private Map<String, String> map(String... strings) {
        assert strings.length % 2 == 0;
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < strings.length; i = i + 2) {
            result.put(strings[i], strings[i + 1]);
        }
        return result;
    }


}
