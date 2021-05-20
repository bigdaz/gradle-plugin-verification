package com.gradle.pluginverifier;

import java.util.ArrayList;
import java.util.List;

public class PluginVersionVerification {
    public final String pluginVersion;
    public VerificationResult validationCheck;
    public VerificationResult eagerTaskCreationCheck;
    public VerificationResult configurationCacheCheck;
    public List<GradleVersionCompatibility> gradleVersionChecks = new ArrayList<>();

    public PluginVersionVerification(String pluginVersion) {
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
}
