package at.porscheinformatik.sonarqube.licensecheck.license;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class License implements Comparable<License> {
    private static final ObjectMapper mapper = new ObjectMapper();
    private String name;
    private String identifier;
    private Boolean status;

    public License() {
    }

    public License(String name, String identifier, Boolean status) {
        super();
        this.name = name;
        this.identifier = identifier;
        this.status = status;
    }

    public static String createString(Collection<License> licensesWithChildren) {
        try {
            return mapper.writeValueAsString(licensesWithChildren.stream().sorted(Comparator.comparing(License::getName)).collect(Collectors.toList()));
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    public static List<License> fromString(String stringValue) {
        try {
            return mapper.readValue(stringValue, new TypeReference<List<License>>() {
            });
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }


    @Override
    public String toString() {
        return "{name:" + name + ", identifier:" + identifier + ", status:" + status + "}";
    }


    @Override
    public int compareTo(License o) {
        if (o == null) {
            return 1;
        } else if (this.identifier.compareTo(o.identifier) == 0) {
            if (this.name.compareTo(o.name) == 0) {
                return this.status.compareTo(o.status);
            } else {
                return this.name.compareTo(o.name);
            }
        } else {
            return this.identifier.compareTo(o.identifier);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        License license = (License) o;
        return Objects.equals(identifier, license.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }
}
