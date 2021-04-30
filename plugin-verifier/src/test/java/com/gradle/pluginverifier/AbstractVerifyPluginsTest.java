package com.gradle.pluginverifier;

import org.gradle.internal.impldep.com.google.common.collect.Lists;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

public abstract class AbstractVerifyPluginsTest {
    @Test
    public void validatePlugin() {
        runBuild("validateExternalPlugins");
    }

    @Test public void validateConfigurationCache() {
        runBuild("--configuration-cache", "clean", getTask());
    }

    @Test public void validateBuildCache() {
        assumeTrue(isIncremental());
        // Run the build with configuration-cache
        String task = getTask();
        runBuild("--no-scan", "clean", task);

        // Without clean, task should be UP-TO-DATE
        BuildResult result = runBuild(task);
        assertEquals(TaskOutcome.UP_TO_DATE, result.task(task).getOutcome());

        // With clean, task should be FROM_CACHE
        result = runBuild("clean", task);
        assertEquals(TaskOutcome.FROM_CACHE, result.task(task).getOutcome());
    }

    private BuildResult runBuild(String... arguments) {
        List<String> args = Lists.newArrayList("-I", "../../init.gradle", "--build-cache");
        args.addAll(Arrays.asList(arguments));

        return GradleRunner.create()
                .forwardOutput()
                .withProjectDir(getProjectDir())
                .withArguments(args)
                .build();
    }

    private File getProjectDir() {
        File root = new File("../verified-plugins");
        return new File(root, getExampleBuild());
    }

    protected abstract String getExampleBuild();

    protected abstract String getTask();

    protected boolean isIncremental() {
        return true;
    }
}
