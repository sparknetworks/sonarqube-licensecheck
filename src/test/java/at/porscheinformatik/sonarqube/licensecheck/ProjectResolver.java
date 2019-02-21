package at.porscheinformatik.sonarqube.licensecheck;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ProjectResolver {
    public static File prepareProject(Supplier<File> projectSource, Consumer<File> postprocessProject) throws IOException {
        File projectRoot;
        projectRoot = new File("target/testProject/");
        FileUtils.deleteDirectory(projectRoot);
        projectRoot.mkdirs();
        final File source = projectSource.get();
        FileUtils.copyDirectory(source, projectRoot);
        postprocessProject.accept(projectRoot);
        return projectRoot.getAbsoluteFile();
    }
}
