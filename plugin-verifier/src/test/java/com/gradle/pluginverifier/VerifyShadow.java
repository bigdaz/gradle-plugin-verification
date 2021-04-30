package com.gradle.pluginverifier;

public class VerifyShadow extends AbstractVerifyPluginsTest {

    @Override
    protected String getExampleBuild() {
        return "com.github.johnrengelman.shadow/shadow-example";
    }

    @Override
    protected String getTask() {
        return ":shadowJar";
    }
}
