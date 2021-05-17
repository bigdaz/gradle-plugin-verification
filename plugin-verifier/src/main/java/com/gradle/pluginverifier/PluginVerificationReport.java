package com.gradle.pluginverifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class PluginVerificationReport {
    public final String pluginId;
    public final List<PluginVersionVerification> pluginVersions = new ArrayList<>();

    public PluginVerificationReport(String pluginId) {
        this.pluginId = pluginId;
    }

    public PluginVersionVerification checkPluginVersion(String pluginVersion) {
        PluginVersionVerification pluginVersionVerification = new PluginVersionVerification(pluginVersion);
        pluginVersions.add(pluginVersionVerification);
        return pluginVersionVerification;
    }

    public void writeJson(File reportFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(reportFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(this);
            writer.write(json);
        }
    }
}
