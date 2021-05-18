package com.gradle.pluginverifier;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
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
        Provider<Directory> samplesWorkingDir = project.getLayout().getBuildDirectory().dir("verified-plugins");
        Provider<Directory> resultsDir = project.getLayout().getBuildDirectory().dir("verification-results");

        TaskProvider<Sync> copySampleInitScripts = project.getTasks().register("copySampleInitScripts", Sync.class, sync -> {
            sync.from(samplesSourceDir);
            sync.include("*-init.gradle");
            sync.into(samplesWorkingDir);
        });

        for (File sample : samplesSourceDir.getAsFile().listFiles(File::isDirectory)) {
            String taskName = "verify_" + sample.getName().replace('.', '_');
            project.getTasks().register(taskName, VerifyPluginTask.class, v -> {
                v.dependsOn(copySampleInitScripts);
                v.getSampleDir().set(sample);
                v.getSampleWorkingDir().set(samplesWorkingDir.map(d -> d.dir(sample.getName())));
                v.getResultsFile().set(resultsDir.map(d -> d.file(sample.getName() + ".json")));
//                v.getPublishBuildScans().set(true);
            });
        }

        project.getTasks().register("verifyPlugins", v -> v.dependsOn(project.getTasks().withType(VerifyPluginTask.class)));
    }
}