package com.gradle.pluginverifier;

public class VerifyDependencyVersions extends AbstractVerifyPluginsTest {

    @Override
    protected String getExampleBuild() {
        return "com.github.ben-manes.versions/versions-example";
    }

    @Override
    protected String getTask() {
        return "dependencyUpdates";
    }
}
