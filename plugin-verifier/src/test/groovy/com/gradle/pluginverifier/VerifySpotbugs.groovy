package com.gradle.pluginverifier

class VerifySpotbugs extends AbstractVerifyPluginsTest {

    @Override
    protected String getPluginId() {
        "com.github.spotbugs"
    }

    @Override
    protected List<String> getPluginVersions() {
        ["4.6.0"]
    }

    @Override
    protected String getTask() {
        ":spotbugsMain"
    }
}
