package com.gradle.pluginverifier;

import java.util.Arrays;
import java.util.List;

public class GradleVersions {
    private static List<String> versions = Arrays.asList("7.0.2", "6.8.3", "5.6.4");

    public static String getLatest() {
        return versions.get(0);
    }

    public static List<String> getAllTested() {
        return versions;
    }
}

