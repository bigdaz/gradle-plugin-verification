package com.gradle.pluginverifier

class VerifyShadow extends AbstractVerifyPluginsTest {

    @Override
    protected String getPluginId() {
        "com.github.johnrengelman.shadow"
    }

    @Override
    protected List<String> getPluginVersions() {
        ["5.2.0", "6.1.0", "7.0.0"]
    }

    @Override
    protected String getTask() {
        ":shadowJar"
    }
}
