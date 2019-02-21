package at.porscheinformatik.sonarqube.licensecheck;

import at.porscheinformatik.sonarqube.licensecheck.model.LicenseDefinition;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

public class LicenseDefinitionTest
{
    private static final String LICENSES_JSON =
        "[{\"name\":\"Apache 2.0\",\"identifier\":\"Apache-2.0\",\"status\":true}," +
            "{\"name\":\"MIT LicenseDefinition\",\"identifier\":\"MIT\",\"status\":false}]";
    private static final String LICENSES_STRING = "Apache 2.0~Apache-2.0~true;MIT LicenseDefinition~MIT~false;";

    private static final LicenseDefinition LIC1 = new LicenseDefinition("Apache 2.0", "Apache-2.0", true);
    private static final LicenseDefinition LIC2 = new LicenseDefinition("MIT LicenseDefinition", "MIT", false);

    @Test
    public void createString()
    {
        String dependenciesJson = LicenseDefinition.createString(asList(LIC2, LIC1));

        assertThat(dependenciesJson, equalTo(LICENSES_JSON));
    }

    @Test
    public void fromStringNew()
    {
        List<LicenseDefinition> licenses = LicenseDefinition.fromString(LICENSES_JSON);

        assertThat(licenses, hasItems(LIC1, LIC2));
    }
}
