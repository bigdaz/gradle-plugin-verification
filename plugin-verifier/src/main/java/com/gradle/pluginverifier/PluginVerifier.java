package com.gradle.pluginverifier;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.gradle.testkit.runner.UnexpectedBuildFailure;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PluginVerifier {
    private final PluginSample plugin;
    private final boolean publishBuildScans;

    public PluginVerifier(PluginSample plugin, boolean publishBuildScans) {
        this.plugin = plugin;
        this.publishBuildScans = publishBuildScans;
    }

    public void runChecks(PrintWriter resultsWriter) {
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
        VerificationResult result = runBuild("--configuration-cache", "clean", plugin.getTask());
        writeResults(resultsWriter, "CONFIGURATION CACHE COMPATIBILITY", result.passed, result.getOutput());
    }

    public void checkBuildCache(PrintWriter resultsWriter) {
        if (!plugin.isIncremental()) {
            return;
        }
        String task = plugin.getTask();

        // Run an initial clean build
        runBuild("--no-scan", "clean", task);

        // Without clean, task should be UP-TO-DATE
        VerificationResult result = runBuild(task);
        writeResults(resultsWriter, "INCREMENTAL BUILD", result.getTaskOutcome(task) == TaskOutcome.UP_TO_DATE, result.getOutput());

        // With clean, task should be FROM_CACHE
        result = runBuild("clean", task);
        writeResults(resultsWriter, "BUILD CACHE", result.getTaskOutcome(task) == TaskOutcome.FROM_CACHE, result.getOutput());
    }

    public void checkVersionCompatibility(PrintWriter resultsWriter) {
        String task = plugin.getTask();
        for (String gradleVersion : GradleVersions.getAllTested()) {
            for (String pluginVersion : plugin.getPluginVersions()) {
                String title = "CHECKING PLUGIN VERSION " + pluginVersion + " WITH GRADLE VERSION " + gradleVersion;

                GradleRunner gradleRunner = gradleRunner(args(pluginVersion, "clean", task)).withGradleVersion(gradleVersion);
                VerificationResult result = runBuild(gradleRunner);
                writeResults(resultsWriter, title, result.passed, result.getOutput());
            }
        }
    }

    private VerificationResult runBuild(String... arguments) {
        List<String> buildArgs = args(plugin.latestPluginVersion(), arguments);
        GradleRunner gradleRunner = gradleRunner(buildArgs);

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

    private List<String> args(String pluginVersion, String... arguments) {
        List<String> argumentList = new ArrayList<>();
        if (publishBuildScans) {
            argumentList.add("-I");
            argumentList.add("../build-scan-init.gradle");
        }
        argumentList.add("--build-cache");
        argumentList.add("-PpluginVersion=" + pluginVersion);
        argumentList.addAll(Arrays.asList(arguments));
        return argumentList;
    }

    private GradleRunner gradleRunner(List<String> arguments) {
        return GradleRunner.create()
                .withProjectDir(plugin.getSampleProject())
                .withArguments(arguments);
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
