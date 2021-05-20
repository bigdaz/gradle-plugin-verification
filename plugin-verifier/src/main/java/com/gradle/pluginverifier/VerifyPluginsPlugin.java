package com.gradle.pluginverifier;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;

public class VerifyPluginsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPlugins().apply("base");

        Directory samplesSourceDir = project.getLayout().getProjectDirectory().dir("verified-plugins");
        Provider<Directory> samplesWorkingDir = project.getLayout().getBuildDirectory().dir("plugins");
        Provider<Directory> resultsDir = project.getLayout().getBuildDirectory().dir("results");

        ConfigurableFileTree initScripts = project.fileTree(samplesSourceDir);
        initScripts.include("*-init.gradle");

        TaskProvider<Sync> copySampleInitScripts = project.getTasks().register("copySampleInitScripts", Sync.class, sync -> {
            sync.from(initScripts);
            sync.into(samplesWorkingDir);
        });

        for (File sample : samplesSourceDir.getAsFile().listFiles(File::isDirectory)) {
            String taskName = "verify_" + sample.getName().replace('.', '_');
            project.getTasks().register(taskName, VerifyPluginTask.class, v -> {
                v.dependsOn(copySampleInitScripts);
                v.getSampleDir().set(sample);
                v.getSamplesWorkingDir().set(samplesWorkingDir);
                v.getResultsFile().set(resultsDir.map(d -> d.file(sample.getName() + ".json")));
                v.getPublishBuildScans().set(true);
                v.getInitScripts().from(initScripts);
            });
        }

        TaskProvider<Task> verifyAllPlugins = project.getTasks().register("verifyAllPlugins", v -> v.dependsOn(project.getTasks().withType(VerifyPluginTask.class)));

        TaskProvider<VerificationReportTask> verificationReport = project.getTasks().register("verificationReport", VerificationReportTask.class, v -> {
            v.mustRunAfter(project.getTasks().withType(VerifyPluginTask.class));
            v.getResultFiles().set(resultsDir);
            v.getReportDir().set(project.getLayout().getBuildDirectory().dir("report"));
        });

        project.getTasks().register("verifyPlugins", t -> {
            t.dependsOn(verifyAllPlugins, verificationReport);
        });
    }
}
