package at.porscheinformatik.sonarqube.licensecheck.gradle;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

class GradleInvoker {

    private static final Logger LOGGER = LoggerFactory.getLogger(GradleInvoker.class);

    private final static String GRADLE_EXEC = "gradle";
    private final static String BUILD_GRADLE = "build.gradle";

    private final File projectRoot;


    // todo: use gradle tooling api if possible
    GradleInvoker(String projectRoot) throws Exception {
        this.projectRoot = new File(projectRoot);
        File buildGradle = new File(projectRoot, BUILD_GRADLE);

        if (!buildGradle.exists()) {
            throw new Exception("no build.gradle found");
        }
    }

    String invoke(String... gradleCommands) throws IOException, GradleInvokerException {
        String[] commands = new String[]{GRADLE_EXEC};
        commands = (String[]) ArrayUtils.addAll(commands, gradleCommands);

        ProcessBuilder processBuilder = new ProcessBuilder();
        Process process = processBuilder.command(commands).directory(projectRoot).start();

        while (process.isAlive()) {
        }
        if (process.exitValue() != 0) {
            LOGGER.error("Failed execution of gradle command {}", Arrays.toString(commands));
            throw new GradleInvokerException("Failed execution of gradle command ");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }

        return builder.toString();
    }

    private class GradleInvokerException extends Exception {
        GradleInvokerException(String message) {
            super(message);
        }
    }
}
