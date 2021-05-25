package com.gradle.pluginverifier;

import org.gradle.util.GradleVersion;

import java.util.Arrays;
import java.util.List;

public class GradleVersions {
    private static final List<String> versions = Arrays.asList(GradleVersion.current().getVersion(), "6.8.3", "5.6.4");

    public static String getLatest() {
        return versions.get(0);
    }

    public static List<String> getAllTested() {
        return versions;
    }
}

