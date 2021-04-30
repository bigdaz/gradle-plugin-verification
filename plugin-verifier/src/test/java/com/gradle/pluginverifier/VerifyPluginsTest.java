package com.gradle.pluginverifier;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class VerifyPluginsTest {
    @Test public void validatePlugin() throws IOException {
        // Run the validateExternalPlugins task
        GradleRunner.create()
            .forwardOutput()
            .withProjectDir(getExampleBuild())
            .withArguments("-I", "../../init.gradle", "validateExternalPlugins")
            .build();
    }

    @Test public void validateConfigurationCache() throws IOException {
        // Run the build with configuration-cache
        GradleRunner.create()
            .forwardOutput()
            .withProjectDir(getExampleBuild())
            .withArguments("-I", "../../init.gradle", "--configuration-cache", "clean", "build")
            .build();
    }

    private File getExampleBuild() {
        File root = new File("../verified-plugins");
        return new File(root, getExampleBuildLocation());
    }

    private String getExampleBuildLocation() {
        return "com.github.spotbugs/spotbugs-example";
    }
}
