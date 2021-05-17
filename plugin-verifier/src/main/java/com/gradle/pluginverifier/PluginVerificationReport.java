package com.gradle.pluginverifier;

import java.util.ArrayList;
import java.util.List;

public class PluginVerificationReport {
    public final PluginSample plugin;
    public VerificationResult pluginValidationCheck;
    public VerificationResult configurationCacheCheck;
    public List<GradleVersionCompatibility> gradleVersionChecks = new ArrayList<>();

    public PluginVerificationReport(PluginSample plugin) {
        this.plugin = plugin;
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
    }

    public interface VerificationResult {
        String getTitle();
        boolean getPassed();
        String getOutput();
    }
}
