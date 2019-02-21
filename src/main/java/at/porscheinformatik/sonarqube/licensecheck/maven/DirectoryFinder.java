package at.porscheinformatik.sonarqube.licensecheck.maven;

import at.porscheinformatik.sonarqube.licensecheck.model.Dependency;

import java.io.File;

class DirectoryFinder {
    private DirectoryFinder() {
    }

    public static File getPomPath(Dependency findPathOfDependency, File mavenRepositoryDir) {
        String[] ids = findPathOfDependency.getName().split(":");
        String groupId = ids[0];
        String artifactId = ids[1];

        String path = groupId.replace(".", "/") +
            "/" + artifactId +
            "/" + findPathOfDependency.getVersion() +
            "/" + artifactId +
            "-" + findPathOfDependency.getVersion() + ".pom";
        return new File(mavenRepositoryDir, path);
    }

    public static File getMavenRepsitoryDir(String userSettings, String globalSettings) {
        if (System.getProperty("maven.repo.local") != null) {
            return new File(System.getProperty("maven.repo.local"));
        }

        File mavenConfFile = new File(System.getProperty("user.home"), ".m2/settings.xml");
        File localRepositoryPath1 = getLocalRepositoryPath(userSettings, mavenConfFile);
        if (localRepositoryPath1 != null) return localRepositoryPath1;

        mavenConfFile = new File(System.getenv("MAVEN_HOME"), "conf/settings.xml");
        File localRepositoryPath = getLocalRepositoryPath(globalSettings, mavenConfFile);
        if (localRepositoryPath != null) return localRepositoryPath;

        return new File(System.getProperty("user.home"), ".m2/repository");
    }

    private static File getLocalRepositoryPath(String userSettings, File mavenConfFile) {
        if (userSettings != null) {
            mavenConfFile = new File(userSettings);
        }
        if (mavenConfFile.exists() && mavenConfFile.isFile()) {
            File localRepositoryPath = SettingsXmlParser.parseXmlFile(mavenConfFile).getLocalRepositoryPath();
            if (localRepositoryPath != null) {
                return localRepositoryPath;
            }
        }
        return null;
    }
}
