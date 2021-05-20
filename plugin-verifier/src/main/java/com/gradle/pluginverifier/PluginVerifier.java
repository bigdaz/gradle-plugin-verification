package com.gradle.pluginverifier;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.gradle.testkit.runner.UnexpectedBuildFailure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PluginVerifier {
    private final PluginSample plugin;
    private final File gradleUserHome;
    private final boolean publishBuildScans;

    public PluginVerifier(PluginSample plugin, File gradleUserHome, boolean publishBuildScans) {
        this.plugin = plugin;
        this.gradleUserHome = gradleUserHome;
        this.publishBuildScans = publishBuildScans;
    }

    public void runChecks(PluginVerificationReport pluginVerificationReport) {
        PluginVersionVerification report = pluginVerificationReport.checkPluginVersion(plugin.getPluginVersion());
        checkPluginValidation(report);
        checkEagerTaskCreation(report);
        checkConfigurationCache(report);
        for (String gradleVersion : GradleVersions.getAllTested()) {
            checkGradleVersionCompatibility(gradleVersion, report);
        }
    }

    /**
     * Plugin validation with the latest version of Gradle.
     */
    public void checkPluginValidation(PluginVersionVerification report) {
        BuildOutcome result = runBuild("--stacktrace", "-I", "../validate-plugin-init.gradle", "validateExternalPlugins");
        report.validationCheck = buildSuccessVerificationResult(result);
    }

    /**
     * Check plugin uses task configuration avoidance with the latest version of Gradle.
     */
    public void checkEagerTaskCreation(PluginVersionVerification report) {
        BuildOutcome result = runBuild("-I", "../validate-plugin-init.gradle", "checkEagerTaskCreation");
        report.eagerTaskCreationCheck = buildSuccessVerificationResult(result);
    }

    /**
     * Check configuration-cache validation with the latest version of Gradle.
     */
    public void checkConfigurationCache(PluginVersionVerification report) {
        BuildOutcome result = runBuild("--configuration-cache", "--no-build-cache", "clean", plugin.getTask());
        report.configurationCacheCheck = buildSuccessVerificationResult(result);
    }

    private void checkGradleVersionCompatibility(String gradleVersion, PluginVersionVerification report) {
        PluginVersionVerification.GradleVersionCompatibility gradleVersionCheck = report.checkGradleVersion(gradleVersion);

        String task = plugin.getTask();

        BuildOutcome result = runBuild(gradleRunner("--build-cache", "clean", task).withGradleVersion(gradleVersion));
        gradleVersionCheck.compatibilityCheck = buildSuccessVerificationResult(result);

        if (result.passed && plugin.isIncremental()) {
            // Without clean, task should be UP-TO-DATE
            result = runBuild(gradleRunner(task).withGradleVersion(gradleVersion));
            gradleVersionCheck.incrementalBuildCheck = taskOutcomeVerificationResult(result, TaskOutcome.UP_TO_DATE);

            // With clean, task should be FROM_CACHE
            result = runBuild(gradleRunner("--build-cache", "clean", task).withGradleVersion(gradleVersion));
            gradleVersionCheck.buildCacheCheck = taskOutcomeVerificationResult(result, TaskOutcome.FROM_CACHE);
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
        argumentList.add("-I");
        argumentList.add("../plugin-version-init.gradle");
        argumentList.add("-DpluginId=" + plugin.getPluginId());
        argumentList.add("-DpluginVersion=" + plugin.getPluginVersion());

        argumentList.add("--gradle-user-home");
        argumentList.add(gradleUserHome.getAbsolutePath());

        argumentList.addAll(Arrays.asList(arguments));

        // Don't bother running 'clean' for non-incremental tasks
        if (!plugin.isIncremental()) {
            argumentList.remove("clean");
        }

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

    private PluginVersionVerification.VerificationResult buildSuccessVerificationResult(BuildOutcome buildOutcome) {
        return new PluginVersionVerification.VerificationResult(buildOutcome.passed, buildOutcome.buildResult.getOutput(), getScanId());
    }

    private PluginVersionVerification.VerificationResult taskOutcomeVerificationResult(BuildOutcome buildOutcome, TaskOutcome expectedOutcome) {
        String task = plugin.getTask();
        task = task.startsWith(":") ? task : ":" + task;
        boolean success = buildOutcome.buildResult.task(task).getOutcome() == expectedOutcome;
        return new PluginVersionVerification.VerificationResult(success, buildOutcome.buildResult.getOutput(), getScanId());
    }

    private String getScanId() {
        if (!publishBuildScans) {
            return null;
        }
        File scanIdFile = new File(plugin.getSampleProject(), "build-scan-id.txt");
        try {
            return Files.readString(scanIdFile.toPath());
        } catch (IOException e) {
            return "ERROR";
        }
    }
}
