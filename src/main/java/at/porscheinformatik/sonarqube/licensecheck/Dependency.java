package at.porscheinformatik.sonarqube.licensecheck;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class Dependency implements Comparable<Dependency> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Dependency.class);

    private String name;
    private String version;
    private String license;
    private String status;
    private String localPath;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public Dependency() {
    }

    public Dependency(String name, String version, String license) {
        super();
        this.name = name;
        this.version = version;
        this.license = license;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((license == null) ? 0 : license.hashCode());
        result = (prime * result) + ((name == null) ? 0 : name.hashCode());
        result = (prime * result) + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Dependency other = (Dependency) obj;
        if (license == null) {
            if (other.license != null) {
                return false;
            }
        } else if (!license.equals(other.license)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "{name:" + name + ", version:" + version + ", license:" + license + ", status:" + status + "}";
    }

    @Override
    public int compareTo(Dependency o) {
        if ((o == null) || (o.name == null)) {
            return 1;
        } else if (this.name == null) {
            return -1;
        }

        return this.name.compareTo(o.name);
    }

    public static List<Dependency> fromString(String serializedDependencyString) {
        try {
            return MAPPER.readValue(serializedDependencyString, new TypeReference<List<Dependency>>() {
            });
        } catch (IOException e) {
            LOGGER.error("Could not deserialize dependencies: ", e);
            return Collections.emptyList();
        }
    }

    public static String createString(Collection<Dependency> dependencies) {
        try {
            return MAPPER.writeValueAsString(new TreeSet<>(dependencies));
        } catch (JsonProcessingException e) {
            LOGGER.error("Could not serialize dependencies: ", e);
            return "";
        }
    }
}
