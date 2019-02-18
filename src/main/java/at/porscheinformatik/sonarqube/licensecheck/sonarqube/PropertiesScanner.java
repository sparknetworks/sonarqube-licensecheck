package at.porscheinformatik.sonarqube.licensecheck.sonarqube;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.sonar.api.config.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PropertiesScanner {

    private PropertiesScanner() {
    }

    public static Stream<Map<String, String>> retrieveMapStream(Configuration configuration, String key, String... fields) {
        return Arrays.stream(configuration.getStringArray(key))
            .map(it ->
                Arrays.stream(fields)
                    .map(field -> ImmutablePair.of(field, configuration.get(constructKey(key, it, field)).orElse(null)))
                    .filter(item -> Objects.nonNull(item.getValue()))
                    .collect(Collectors.toMap(Pair::getKey, Pair::getValue, (a, b) -> a))
            );
    }

    public static String constructKey(String... elements) {
        return StringUtils.join(elements, ".");
    }

    public static List<String> retrieveStringList(Configuration configuration, String key, String field) {
        return retrieveMapStream(configuration, key, field)
            .map(item -> item.get(field))
            .collect(Collectors.toList());
    }
}
