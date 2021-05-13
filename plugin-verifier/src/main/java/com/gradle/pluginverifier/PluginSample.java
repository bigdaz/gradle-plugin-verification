package com.gradle.pluginverifier;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PluginSample {
    private final File sampleDir;
    private final String pluginVersion;
    private final String task;
    private final boolean incremental;

    public PluginSample(File sampleDir, String pluginVersion, String task, boolean incremental) {
        this.sampleDir = sampleDir;
        this.pluginVersion = pluginVersion;
        this.task = task;
        this.incremental = incremental;
    }

    public String getPluginId() {
        return sampleDir.getName();
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public String getTask() {
        return task;
    }

    public boolean isIncremental() {
        return incremental;
    }

    public File getSampleProject() {
        return sampleDir;
    }
}

