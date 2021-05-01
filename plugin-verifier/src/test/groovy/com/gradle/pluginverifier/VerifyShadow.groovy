package com.gradle.pluginverifier

class VerifyShadow extends AbstractVerifyPluginsTest {

    @Override
    protected String getExampleBuild() {
        return "com.github.johnrengelman.shadow/shadow-example"
    }

    @Override
    protected String getTask() {
        return ":shadowJar"
    }

    @Override
    protected List<String> getPluginVersions() {
        ["5.2.0", "6.1.0", "7.0.0"]
    }
}
