package com.gradle.pluginverifier

class VerifyDependencyVersions extends AbstractVerifyPluginsTest {

    @Override
    protected String getPluginId() {
        "com.github.ben-manes.versions"
    }

    @Override
    protected List<String> getPluginVersions() {
        ["0.36.0"]
    }

    @Override
    protected String getTask() {
        ":dependencyUpdates"
    }

    protected static boolean isIncremental() {
        return false;
    }
}
