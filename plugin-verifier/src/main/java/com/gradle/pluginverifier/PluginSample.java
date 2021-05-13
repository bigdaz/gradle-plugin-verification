package com.gradle.pluginverifier;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PluginSample {
    private final File sampleDir;
    private final List<String> pluginVersions;
    private final String task;
    private final boolean incremental;

    public PluginSample(File sampleDir, List<String> pluginVersions, String task, boolean incremental) {
        this.sampleDir = sampleDir;
        this.pluginVersions = new ArrayList<>(pluginVersions);
        this.pluginVersions.sort(Comparator.reverseOrder());
        this.task = task;
        this.incremental = incremental;
    }

    public String getPluginId() {
        return sampleDir.getName();
    }

    public List<String> getPluginVersions() {
        return pluginVersions;
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

    public String latestPluginVersion() {
        return pluginVersions.get(0);
    }
}

