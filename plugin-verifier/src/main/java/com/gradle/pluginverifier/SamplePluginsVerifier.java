package com.gradle.pluginverifier;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class SamplePluginsVerifier {
    public static void main(String[] args) {
        PluginSample dependenciesPlugin = new PluginSample(
                sampleDir("com.github.ben-manes.versions"),
                versions("0.36.0"),
                ":dependencyUpdates",
                false
        );
        PluginSample shadowPlugin = new PluginSample(
                sampleDir("com.github.johnrengelman.shadow"),
                versions("5.2.0", "6.1.0", "7.0.0"),
                ":shadowJar"
        );

        new PluginVerifier(shadowPlugin).runChecks();
    }

    private static List<String> versions(String... versions) {
        return Arrays.asList(versions);
    }

    private static File sampleDir(String pluginId) {
        return new File("build/verified-plugins", pluginId);
    }
}
