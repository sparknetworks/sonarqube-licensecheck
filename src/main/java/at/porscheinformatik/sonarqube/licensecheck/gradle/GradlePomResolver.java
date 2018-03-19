package at.porscheinformatik.sonarqube.licensecheck.gradle;

import at.porscheinformatik.sonarqube.licensecheck.gradle.model.PomProject;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class GradlePomResolver {

    private final File projectRoot;

    GradlePomResolver(File projectRoot) {
        this.projectRoot = projectRoot;
    }

    List<PomProject> resolvePoms() throws Exception {
        GradleInvoker gradleInvoker = new GradleInvoker(projectRoot.getAbsolutePath());

        gradleInvoker.invoke("copyPoms", "-I", createInitScript());

        String relativePoms = "build/poms";
        File targetDir = new File(projectRoot, relativePoms);
        assert targetDir.exists();

        return parsePomsInDir(targetDir);
    }

    private List<PomProject> parsePomsInDir(File targetDir) {
        Collection<File> pomFiles = FileUtils.listFiles(targetDir, new String[]{"pom"}, false);

        return pomFiles.stream()
            .map(File::getAbsolutePath)
            .map(this::mapToPom)
            .collect(Collectors.toList());
    }

    private PomProject mapToPom(String pomPath) {
        File file = new File(pomPath);
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String xml = null;
        try {
            xml = FileUtils.readFileToString(file);
            return xmlMapper.readValue(xml, PomProject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String createInitScript() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("gradle/pom.gradle");
        File file = new File(projectRoot, "pom.gradle");
        try {
            FileUtils.copyInputStreamToFile(inputStream, file);
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
