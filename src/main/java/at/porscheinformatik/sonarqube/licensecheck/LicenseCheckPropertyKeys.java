package at.porscheinformatik.sonarqube.licensecheck;

public class LicenseCheckPropertyKeys {

    private LicenseCheckPropertyKeys() {}

    public static final String NAME_MATCHES = "nameMatches";
    public static final String LICENSE = "license";
    public static final String NAME = "name";

    public static final String LICENSE_WHITELIST_KEY = "licensecheck.licenses_whitelist";

    public static final String LICENSE_BLACKLIST_KEY = "licensecheck.licenses_blacklist";

    public static final String LICENSE_BLACKLIST_DEFAULT_KEY = "licensecheck.licenses_blacklist_default";

    public static final String LICENSE_REGEX = "licensecheck.licensesregex";

    public static final String MAVEN_REGEX = "licensecheck.mavenregex";

    public static final String INTERNAL_REGEX = "licensecheck.internalregex";

    public static final String ACTIVATION_KEY = "licensecheck.activation";

    public static final String FORBID_UNKNOWN = "licensecheck.forbid_unknown";
}
