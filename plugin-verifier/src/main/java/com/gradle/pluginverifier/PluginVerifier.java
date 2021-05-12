package com.gradle.pluginverifier;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PluginVerifier {
    private static final List<String> FIXED_ARGS = Arrays.asList("-I", "../build-scan-init.gradle", "--build-cache");
    private final PluginSample plugin;

    public PluginVerifier(PluginSample plugin) {
        this.plugin = plugin;
    }

    public void runChecks() {
        checkPluginValidation();
        checkConfigurationCache();
        if (plugin.isIncremental()) {
            checkBuildCache();
        }
        checkVersionCompatibility();
    }

    public void checkPluginValidation() {
        System.out.println("CHECKING PLUGIN VALIDATION");
        runBuild("-I", "../validate-plugin-init.gradle", "validateExternalPlugins");
    }

    public void checkConfigurationCache() {
        System.out.println("CHECKING CONFIGURATION CACHE COMPATIBILITY");
        runBuild("--configuration-cache", "clean", plugin.getTask());
    }

    public void checkBuildCache() {
        System.out.println("CHECKING INCREMENTAL BUILD AND BUILD CACHE");
        String task = plugin.getTask();

        // Run an initial clean build
        runBuild("--no-scan", "clean", task);

        // Without clean, task should be UP-TO-DATE
        BuildResult result = runBuild(task);
        assert result.task(task).getOutcome() == TaskOutcome.UP_TO_DATE;

        // With clean, task should be FROM_CACHE
        result = runBuild("clean", task);
        assert result.task(task).getOutcome() == TaskOutcome.FROM_CACHE;
    }

    public void checkVersionCompatibility() {
        String task = plugin.getTask();
        for (String gradleVersion : GradleVersions.getAllTested()) {
            for (String pluginVersion : plugin.getPluginVersions()) {
                System.out.println("CHECKING PLUGIN VERSION " + pluginVersion + " WITH GRADLE VERSION " + gradleVersion);
                gradleRunner(args(pluginVersion, "clean", task)).withGradleVersion(gradleVersion).build();
            }
        }
    }

    private BuildResult runBuild(String... arguments) {
        return gradleRunner(args(plugin.latestPluginVersion(), arguments)).build();
    }

    private List<String> args(String pluginVersion, String... arguments) {
        List<String> argumentList = new ArrayList<>();
        argumentList.addAll(FIXED_ARGS);
        argumentList.add("-PpluginVersion=" + pluginVersion);
        argumentList.addAll(Arrays.asList(arguments));
        return argumentList;
    }

    private GradleRunner gradleRunner(List<String> arguments) {
        return GradleRunner.create()
                .forwardOutput()
                .withProjectDir(plugin.getSampleProject())
                .withArguments(arguments);
    }

}
