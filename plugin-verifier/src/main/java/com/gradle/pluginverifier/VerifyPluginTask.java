package com.gradle.pluginverifier;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public abstract class VerifyPluginTask extends DefaultTask {
    @InputDirectory
    public abstract DirectoryProperty getSampleDir();

    @Input
    public abstract Property<Boolean> getPublishBuildScans();

    public VerifyPluginTask() {
        getPublishBuildScans().convention(false);
    }

    @TaskAction
    public void verifyPlugin() {
        Properties props = loadSampleProperties();
        String sampleTask = props.getProperty("task");
        List<String> pluginVersions = Arrays.stream(props.getProperty("pluginVersions").split(",")).map(String::trim).collect(Collectors.toList());
        boolean incremental = Boolean.parseBoolean(props.getProperty("incremental", "true"));

        PluginSample pluginSample = new PluginSample(getSampleDir().get().getAsFile(), pluginVersions, sampleTask, incremental);
        new PluginVerifier(pluginSample, getPublishBuildScans().get()).runChecks();
    }

    private Properties loadSampleProperties() {
        try (InputStream input = new FileInputStream(getSampleDir().file("plugin-verification.properties").get().getAsFile())) {
            Properties props = new Properties();
            props.load(input);
            return props;

        } catch (IOException ex) {
            throw new GradleException("Could not load 'plugin-verification.properties' for sample", ex);
        }
    }
}