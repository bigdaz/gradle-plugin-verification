package com.gradle.pluginverifier;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
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

    @Internal
    public abstract DirectoryProperty getSampleWorkingDir();

    @Input
    public abstract Property<Boolean> getPublishBuildScans();

    @OutputFile
    public abstract RegularFileProperty getResultsFile();

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
        copySampleToWorkingDir();

        Properties props = loadSampleProperties();
        String sampleTask = props.getProperty("task");
        List<String> pluginVersions = Arrays.stream(props.getProperty("pluginVersions").split(",")).map(String::trim).collect(Collectors.toList());
        boolean incremental = Boolean.parseBoolean(props.getProperty("incremental", "true"));

        File pluginSampleDir = getSampleWorkingDir().get().getAsFile();
        String pluginId = pluginSampleDir.getName();

        PluginVerificationReport report = new PluginVerificationReport(pluginId);
        for (String pluginVersion : pluginVersions) {
            PluginSample pluginSample = new PluginSample(pluginSampleDir, pluginVersion, sampleTask, incremental);
            PluginVerifier pluginVerifier = new PluginVerifier(pluginSample, getSampleGradleUserHome().get().getAsFile(), getPublishBuildScans().get());
            pluginVerifier.runChecks(report);
        }
        report.writeJson(getResultsFile().get().getAsFile());
    }

    private void copySampleToWorkingDir() {
        getFileSystemOperations().copy(copySpec -> {
            copySpec.from(getSampleDir());
            copySpec.into(getSampleWorkingDir());
        });
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
