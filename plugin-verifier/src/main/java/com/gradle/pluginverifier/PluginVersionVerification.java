package com.gradle.pluginverifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.gradle.internal.impldep.com.google.common.io.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class PluginVersionVerification {
    public final String pluginId;
    public final String pluginVersion;
    public VerificationResult validationCheck;
    public VerificationResult eagerTaskCreationCheck;
    public VerificationResult configurationCacheCheck;
    public List<GradleVersionCompatibility> gradleVersionChecks = new ArrayList<>();

    public PluginVersionVerification(String pluginId, String pluginVersion) {
        this.pluginId = pluginId;
        this.pluginVersion = pluginVersion;
    }

    public GradleVersionCompatibility checkGradleVersion(String gradleVersion) {
        GradleVersionCompatibility compatibility = new GradleVersionCompatibility(gradleVersion);
        gradleVersionChecks.add(compatibility);
        return compatibility;
    }

    public static class GradleVersionCompatibility {
        public final String gradleVersion;

        private GradleVersionCompatibility(String gradleVersion) {
            this.gradleVersion = gradleVersion;
        }

        public VerificationResult compatibilityCheck;
        public VerificationResult incrementalBuildCheck;
        public VerificationResult buildCacheCheck;
        public VerificationResult relocatedBuildCacheCheck;
    }

    public static class VerificationResult {
        public final boolean passed;
        public final String output;
        public final String buildScanId;

        public VerificationResult(boolean passed, String output, String buildScanId) {
            this.passed = passed;
            this.output = output;
            this.buildScanId = buildScanId;
        }
    }

    public void toJson(File reportFile) throws FileNotFoundException {
        reportFile.getParentFile().mkdirs();
        try (PrintWriter writer = new PrintWriter(reportFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(this);
            writer.write(json);
        }
    }

    public static PluginVersionVerification fromJson(File resultFile) throws FileNotFoundException {
        return new Gson().fromJson(Files.newReader(resultFile, Charset.defaultCharset()), PluginVersionVerification.class);
    }
}
