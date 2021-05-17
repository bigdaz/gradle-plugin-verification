package com.gradle.pluginverifier;

public interface PluginVerificationResult {
    String getTitle();
    boolean getPassed();
    String getOutput();
}
