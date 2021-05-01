package com.gradle.pluginverifier

import spock.lang.Ignore;

class VerifyDependencyVersions extends AbstractVerifyPluginsTest {

    @Override
    protected String getExampleBuild() {
        return "com.github.ben-manes.versions/versions-example"
    }

    @Override
    protected String getTask() {
        return ":dependencyUpdates"
    }

    protected static boolean isIncremental() {
        return false;
    }
}
