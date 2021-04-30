package com.gradle.pluginverifier;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class VerifyPluginsTest {
    @Test public void runPluginValidation() throws IOException {
        File projectDir = new File("/Users/daz/dev/playground/plugin-verification/verified-plugins/com.github.spotbugs/spotbugs-example");

        // Run the build
        BuildResult result = GradleRunner.create()
            .forwardOutput()
            .withProjectDir(projectDir)
            .withArguments("validateExternalPlugins")
            .build();
    }
}
