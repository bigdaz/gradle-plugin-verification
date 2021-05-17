package com.gradle.pluginverifier;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.gradle.testkit.runner.UnexpectedBuildFailure;

import java.io.File;
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

    public void runChecks(PluginVerificationReport report) {
        report.writeHeader(plugin);
        checkPluginValidation(report);
        checkConfigurationCache(report);
        for (String gradleVersion : GradleVersions.getAllTested()) {
            checkGradleVersionCompatibility(gradleVersion, report);
        }
    }

    /**
     * Plugin validation with the latest version of Gradle.
     */
    public void checkPluginValidation(PluginVerificationReport report) {
        VerificationResult result = runBuild("-I", "../validate-plugin-init.gradle", "validateExternalPlugins");
        report.writeResults("PLUGIN VALIDATION", result.passed, result.getOutput());
    }

    /**
     * Check configuration-cache validation with the latest version of Gradle.
     */
    public void checkConfigurationCache(PluginVerificationReport report) {
        VerificationResult result = runBuild("--configuration-cache", "--no-build-cache", "clean", plugin.getTask());
        report.writeResults("CONFIGURATION CACHE COMPATIBILITY", result.passed, result.getOutput());
    }

    private void checkGradleVersionCompatibility(String gradleVersion, PluginVerificationReport report) {
        String task = plugin.getTask();

        VerificationResult result = runBuild(gradleRunner("clean", task).withGradleVersion(gradleVersion));
        report.writeResults("COMPATIBLE with GRADLE " + gradleVersion, result.passed, result.getOutput());

        if (result.passed && plugin.isIncremental()) {
            // Without clean, task should be UP-TO-DATE
            result = runBuild(gradleRunner(task).withGradleVersion(gradleVersion));
            boolean passed = result.passed && result.getTaskOutcome(task) == TaskOutcome.UP_TO_DATE;
            report.writeResults("INCREMENTAL BUILD with GRADLE " + gradleVersion, passed, result.getOutput());

            // With clean, task should be FROM_CACHE
            result = runBuild(gradleRunner("--build-cache", "clean", task).withGradleVersion(gradleVersion));
            passed = result.passed && result.getTaskOutcome(task) == TaskOutcome.FROM_CACHE;
            report.writeResults("BUILD CACHE with GRADLE " + gradleVersion, passed, result.getOutput());
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
