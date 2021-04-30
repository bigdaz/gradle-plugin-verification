package com.gradle.pluginverifier;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public abstract class AbstractVerifyPluginsTest {
    @Test
    public void validatePlugin() throws IOException {
        // Run the validateExternalPlugins task
        GradleRunner.create()
            .forwardOutput()
            .withProjectDir(getProjectDir())
            .withArguments("-I", "../../init.gradle", "validateExternalPlugins")
            .build();
    }

    @Test public void validateConfigurationCache() throws IOException {
        // Run the build with configuration-cache
        GradleRunner.create()
            .forwardOutput()
            .withProjectDir(getProjectDir())
            .withArguments("-I", "../../init.gradle", "--configuration-cache", "clean", getTask())
            .build();
    }

    private File getProjectDir() {
        File root = new File("../verified-plugins");
        return new File(root, getExampleBuild());
    }

    protected abstract String getExampleBuild();

    protected abstract String getTask();
}
