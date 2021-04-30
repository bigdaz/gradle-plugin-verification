package com.gradle.pluginverifier;

public class VerifySpotbugs extends AbstractVerifyPluginsTest {

    @Override
    protected String getExampleBuild() {
        return "com.github.spotbugs/spotbugs-example";
    }
}
