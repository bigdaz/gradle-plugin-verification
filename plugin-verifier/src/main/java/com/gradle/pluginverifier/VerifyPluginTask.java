package com.gradle.pluginverifier;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@CacheableTask
public abstract class VerifyPluginTask extends DefaultTask {
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getSampleDir();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getInitScripts();

    @Input
    public abstract Property<Boolean> getPublishBuildScans();

    @OutputDirectory
    public abstract DirectoryProperty getResultsDir();

    @Internal
    public abstract DirectoryProperty getSamplesWorkingDir();

    @Internal
    public abstract DirectoryProperty getSampleGradleUserHome();

    @Inject
    public VerifyPluginTask(ProjectLayout projectLayout) {
        getPublishBuildScans().convention(false);
        getSampleGradleUserHome().convention(projectLayout.getBuildDirectory().dir("gradle-user-home").get());
    }

    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();

    @TaskAction
    public void verifyPlugin() throws IOException {
        Properties props = loadSampleProperties();
        String sampleTask = props.getProperty("task");
        List<String> pluginVersions = Arrays.stream(props.getProperty("pluginVersions").split(",")).map(String::trim).collect(Collectors.toList());
        boolean incremental = Boolean.parseBoolean(props.getProperty("incremental", "true"));

        File pluginSampleDir = getSampleDir().get().getAsFile();
        String pluginId = pluginSampleDir.getName();

        File pluginResultsDir = getResultsDir().dir(pluginId).get().getAsFile();

        for (String pluginVersion : pluginVersions) {
            PluginVersionVerification report = new PluginVersionVerification(pluginId, pluginVersion);
            PluginSample pluginSample = new PluginSample(pluginSampleDir, pluginVersion, sampleTask, incremental);
            PluginVerifier pluginVerifier = new PluginVerifier(pluginSample, getSamplesWorkingDir().get().getAsFile(), getSampleGradleUserHome().get().getAsFile(), getPublishBuildScans().get(), getFileSystemOperations());
            pluginVerifier.runChecks(report);

            // TODO:DAZ Avoid regenerating existing result file.
            File reportFile = new File(pluginResultsDir, pluginId + "_" + pluginVersion + ".json");
            report.toJson(reportFile);
        }
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
