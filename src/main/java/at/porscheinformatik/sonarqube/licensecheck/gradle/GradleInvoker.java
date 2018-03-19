package at.porscheinformatik.sonarqube.licensecheck.gradle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.stream.Stream;

class GradleInvoker {

    private static final Logger LOGGER = LoggerFactory.getLogger(GradleInvoker.class);

    // todo: dynamically resolve gradle executable (wrapper if available)
    private final static String GRADLE_EXEC = "gradle";
    private final static String BUILD_GRADLE = "build.gradle";

    private final File projectRoot;

    private String userSettings;


    // todo: use gradle tooling api if possible
    GradleInvoker(String projectRoot) throws Exception {
        this.projectRoot = new File(projectRoot);
        File buildGradle = new File(projectRoot, BUILD_GRADLE);

        if (!buildGradle.exists()) {
            throw new Exception("no build.gradle found");
        }

        userSettings = null;
        CommandLine cmd = getCommandLineArgs();
        if (cmd != null) {
            if (cmd.hasOption("I")) {
                userSettings = cmd.getOptionValue("I");
            }
        }
    }

    // todo: add additional script support via custom -I
    String invoke(String... gradleCommands) throws IOException, GradleInvokerException {

        // todo: clean this mess up
        String[] baseCommands;
        if (userSettings == null) {
            baseCommands = new String[]{GRADLE_EXEC, "-i"};
        } else {
            String[] split = userSettings.split(" ");
            baseCommands = new String[split.length + 2];
            baseCommands[0] = GRADLE_EXEC;
            baseCommands[1] = "-i";
            baseCommands[2] = split[0];
            baseCommands[3] = split[1];
        }
        String[] commands = Stream
            .of(baseCommands, gradleCommands)
            .flatMap(Stream::of)
            .toArray(String[]::new);

        ProcessBuilder processBuilder = new ProcessBuilder();
        Process process = processBuilder.command(commands).directory(projectRoot).start();

        InputStream errorStream = process.getErrorStream();
        InputStream inputStream = process.getInputStream();
        String stderr = getOutput(errorStream);
        String stdout = getOutput(inputStream);

        while (process.isAlive()) {
        }
        if (process.exitValue() != 0) {
            LOGGER.error("Failed execution of gradle command {}", Arrays.toString(commands));
            LOGGER.error("Gradle stdout: {}", stderr);
            throw new GradleInvokerException("Failed execution of gradle command ");
        }

        return stdout;
    }

    private String getOutput(InputStream errorStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
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

    private static CommandLine getCommandLineArgs() {
        CommandLine cmd = null;
        try {
            String commandArgs = System.getProperty("sun.java.command");
            CommandLineParser parser = new DefaultParser();
            Options options = new Options();
            options.addOption("I", "--init-script", true, "Specifies an initialization script.");
            cmd = parser.parse(options, commandArgs.split(" "));
        } catch (Exception e) {
            // ignore unparsable command line args
        }
        return cmd;
    }

}
