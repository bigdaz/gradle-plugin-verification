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

    public void runChecks(PluginVerificationReportWriter report) {
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
    public void checkPluginValidation(PluginVerificationReportWriter report) {
        BuildOutcome result = runBuild("-I", "../validate-plugin-init.gradle", "validateExternalPlugins");
        report.writeResults(new BuildSuccessVerificationResult("PLUGIN VALIDATION", result));
    }

    /**
     * Check configuration-cache validation with the latest version of Gradle.
     */
    public void checkConfigurationCache(PluginVerificationReportWriter report) {
        BuildOutcome result = runBuild("--configuration-cache", "--no-build-cache", "clean", plugin.getTask());
        report.writeResults(new BuildSuccessVerificationResult("CONFIGURATION CACHE COMPATIBILITY", result));
    }

    private void checkGradleVersionCompatibility(String gradleVersion, PluginVerificationReportWriter report) {
        String task = plugin.getTask();

        BuildOutcome result = runBuild(gradleRunner("clean", task).withGradleVersion(gradleVersion));
        report.writeResults(new BuildSuccessVerificationResult("COMPATIBLE with GRADLE " + gradleVersion, result));

        if (result.passed && plugin.isIncremental()) {
            // Without clean, task should be UP-TO-DATE
            result = runBuild(gradleRunner(task).withGradleVersion(gradleVersion));
            report.writeResults(new TaskOutcomeVerificationResult("INCREMENTAL BUILD with GRADLE " + gradleVersion, result, task, TaskOutcome.UP_TO_DATE));

            // With clean, task should be FROM_CACHE
            result = runBuild(gradleRunner("--build-cache", "clean", task).withGradleVersion(gradleVersion));
            report.writeResults(new TaskOutcomeVerificationResult("BUILD CACHE with GRADLE " + gradleVersion, result, task, TaskOutcome.FROM_CACHE));
        }
    }

    private BuildOutcome runBuild(String... arguments) {
        GradleRunner gradleRunner = gradleRunner(arguments);
        return runBuild(gradleRunner);
    }

    private BuildOutcome runBuild(GradleRunner gradleRunner) {
        try {
            BuildResult result = gradleRunner.build();
            return new BuildOutcome(true, result);
        } catch (UnexpectedBuildFailure unexpectedBuildFailure) {
            return new BuildOutcome(false, unexpectedBuildFailure.getBuildResult());
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

    private static class BuildOutcome {
        public final boolean passed;
        public final BuildResult buildResult;

        private BuildOutcome(boolean passed, BuildResult buildResult) {
            this.passed = passed;
            this.buildResult = buildResult;
        }
    }

    private static class BuildSuccessVerificationResult implements PluginVerificationResult {
        private final String title;
        private final BuildOutcome buildOutcome;

        private BuildSuccessVerificationResult(String title, BuildOutcome buildOutcome) {
            this.title = title;
            this.buildOutcome = buildOutcome;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public boolean getPassed() {
            return buildOutcome.passed;
        }

        @Override
        public String getOutput() {
            return buildOutcome.buildResult.getOutput();
        }
    }

    private static class TaskOutcomeVerificationResult implements PluginVerificationResult {
        private final String title;
        private final BuildOutcome buildOutcome;
        private final String task;
        private final TaskOutcome expectedOutcome;

        private TaskOutcomeVerificationResult(String title, BuildOutcome buildOutcome, String task, TaskOutcome expectedOutcome) {
            this.title = title;
            this.buildOutcome = buildOutcome;
            this.task = task;
            this.expectedOutcome = expectedOutcome;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public boolean getPassed() {
            return buildOutcome.buildResult.task(task).getOutcome() == expectedOutcome;
        }

        @Override
        public String getOutput() {
            return buildOutcome.buildResult.getOutput();
        }
    }

}
