package at.porscheinformatik.sonarqube.licensecheck.sonarqube;

import org.sonar.api.config.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class SonarqubeConfigurationHelper {
    public static void mockConfiguration(Configuration mock, String parentKey, Map<String, String>... entries) {
        String[] keys = new String[entries.length];
        for (int i = 0; i < entries.length; i++) {
            Map<String, String> entry = entries[i];
            final String index = String.valueOf(i);
            entry.forEach((key, value) -> when(mock.get(PropertiesReader.constructKey(parentKey, index, key))).thenReturn(Optional.of(value)));
            keys[i] = index;
        }
        when(mock.getStringArray(parentKey)).thenReturn(keys);
    }

    public static void mockConfiguration(Configuration mock) {
        when(mock.get(any())).thenReturn(Optional.empty());
    }

    public static Map<String, String> map(String... strings) {
        assert strings.length % 2 == 0;
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < strings.length; i = i + 2) {
            result.put(strings[i], strings[i + 1]);
        }
        return result;
    }
}
