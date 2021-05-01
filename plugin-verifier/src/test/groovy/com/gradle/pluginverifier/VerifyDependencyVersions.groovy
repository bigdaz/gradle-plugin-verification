package com.gradle.pluginverifier

class VerifyDependencyVersions extends AbstractVerifyPluginsTest {

    @Override
    protected String getExampleBuild() {
        return "com.github.ben-manes.versions/versions-example"
    }

    @Override
    protected String getTask() {
        return ":dependencyUpdates"
    }

    @Override
    protected List<String> getPluginVersions() {
        ["0.36.0"]
    }

    protected static boolean isIncremental() {
        return false;
    }
}
