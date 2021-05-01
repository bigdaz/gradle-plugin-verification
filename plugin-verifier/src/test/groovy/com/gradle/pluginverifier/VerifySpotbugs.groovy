package com.gradle.pluginverifier

class VerifySpotbugs extends AbstractVerifyPluginsTest {

    @Override
    protected String getExampleBuild() {
        return "com.github.spotbugs/spotbugs-example"
    }

    @Override
    protected String getTask() {
        return ":spotbugsMain"
    }
}
