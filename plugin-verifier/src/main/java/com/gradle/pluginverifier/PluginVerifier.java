package com.gradle.pluginverifier;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.gradle.testkit.runner.UnexpectedBuildFailure;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PluginVerifier {
    private final PluginSample plugin;
    private final File workingDir;
    private final boolean publishBuildScans;

    public PluginVerifier(PluginSample plugin, File workingDir, boolean publishBuildScans) {
        this.plugin = plugin;
        this.workingDir = workingDir;
        this.publishBuildScans = publishBuildScans;
    }

    public void runChecks(PrintWriter resultsWriter) {
        writeHeader(resultsWriter);
        checkPluginValidation(resultsWriter);
        checkConfigurationCache(resultsWriter);
        if (plugin.isIncremental()) {
            checkBuildCache(resultsWriter);
        }
        checkVersionCompatibility(resultsWriter);
    }

    /**
     * Plugin validation with the latest version of Gradle.
     */
    public void checkPluginValidation(PrintWriter resultWriter) {
        VerificationResult result = runBuild("-I", "../validate-plugin-init.gradle", "validateExternalPlugins");
        writeResults(resultWriter, "PLUGIN VALIDATION", result.passed, result.getOutput());
    }

    /**
     * Check configuration-cache validation with the latest version of Gradle.
     */
    public void checkConfigurationCache(PrintWriter resultsWriter) {
        VerificationResult result = runBuild("--configuration-cache", "--no-build-cache", "clean", plugin.getTask());
        writeResults(resultsWriter, "CONFIGURATION CACHE COMPATIBILITY", result.passed, result.getOutput());
    }

    public void checkBuildCache(PrintWriter resultsWriter) {
        if (!plugin.isIncremental()) {
            return;
        }
        String task = plugin.getTask();

        // Run an initial clean build and populate the build cache
        runBuild("--no-scan", "--build-cache", "clean", task);

        // Without clean, task should be UP-TO-DATE
        VerificationResult result = runBuild(task);
        boolean passed = result.passed && result.getTaskOutcome(task) == TaskOutcome.UP_TO_DATE;
        writeResults(resultsWriter, "INCREMENTAL BUILD", passed, result.getOutput());

        // With clean, task should be FROM_CACHE
        result = runBuild("--build-cache", "clean", task);
        passed = result.passed && result.getTaskOutcome(task) == TaskOutcome.FROM_CACHE;
        writeResults(resultsWriter, "BUILD CACHE", passed, result.getOutput());
    }

    public void checkVersionCompatibility(PrintWriter resultsWriter) {
        String task = plugin.getTask();
        for (String gradleVersion : GradleVersions.getAllTested()) {
            VerificationResult result = runBuild(gradleRunner("clean", task).withGradleVersion(gradleVersion));
            writeResults(resultsWriter, "COMPATIBLE with GRADLE " + gradleVersion, result.passed, result.getOutput());
        }
    }

    private VerificationResult runBuild(String... arguments) {
        GradleRunner gradleRunner = gradleRunner(arguments);
        return runBuild(gradleRunner);
    }

    private VerificationResult runBuild(GradleRunner gradleRunner) {
        try {
            BuildResult result = gradleRunner.build();
            return new VerificationResult(true, result);
        } catch (UnexpectedBuildFailure unexpectedBuildFailure) {
            return new VerificationResult(false, unexpectedBuildFailure.getBuildResult());
        }
    }

    private GradleRunner gradleRunner(String... arguments) {
        List<String> argumentList = new ArrayList<>();
        if (publishBuildScans) {
            argumentList.add("-I");
            argumentList.add("../build-scan-init.gradle");
        }
        argumentList.add("--gradle-user-home");
        argumentList.add(new File(workingDir, "gradle-user-home").getAbsolutePath());
        argumentList.add("-PpluginVersion=" + plugin.getPluginVersion());
        argumentList.addAll(Arrays.asList(arguments));

        return GradleRunner.create()
                .withProjectDir(plugin.getSampleProject())
                .withArguments(argumentList);
    }

    private void writeHeader(PrintWriter resultWriter) {
        resultWriter.println("=================================");
        resultWriter.println("Plugin verification: " + plugin.getPluginId() + ":" + plugin.getPluginVersion());
        resultWriter.println("=================================");
    }

    private void writeResults(PrintWriter resultWriter, String title, boolean passed, String output) {
        resultWriter.println("--------------------------");
        resultWriter.println(title);
        resultWriter.println(passed ? "SUCCESS" : "FAILED");
        resultWriter.println("--------------------------");
        resultWriter.print(output);
        resultWriter.println("--------------------------");
        resultWriter.println();
    }

    private static class VerificationResult {
        public final boolean passed;
        public final BuildResult buildResult;

        private VerificationResult(boolean passed, BuildResult buildResult) {
            this.passed = passed;
            this.buildResult = buildResult;
        }

        public String getOutput() {
            return buildResult.getOutput();
        }

        public TaskOutcome getTaskOutcome(String task) {
            return buildResult.task(task).getOutcome();
        }
    }

}
