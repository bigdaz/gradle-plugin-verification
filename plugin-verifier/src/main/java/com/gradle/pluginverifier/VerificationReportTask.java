package com.gradle.pluginverifier;

import com.google.gson.Gson;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.impldep.com.google.common.io.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

public abstract class VerificationReportTask extends DefaultTask {
    @InputDirectory
    public abstract DirectoryProperty getResultFiles();

    @OutputDirectory
    public abstract DirectoryProperty getReportDir();

    @TaskAction
    public void generate() throws FileNotFoundException {
        File reportFile = getReportDir().file("index.html").get().getAsFile();
        PrintWriter writer = new PrintWriter(reportFile);
        writer.println("<html><body>");

        for (File resultFile : getResultFiles().get().getAsFile().listFiles()) {
            Gson gson = new Gson();
            PluginVerificationReport pluginReport = gson.fromJson(Files.newReader(resultFile, Charset.defaultCharset()), PluginVerificationReport.class);
            writer.println("<h1>Plugin: " + pluginReport.pluginId + "</h1>");
            for (PluginVersionVerification versionReport : pluginReport.pluginVersions) {
                writer.println("<h2>Version: " + versionReport.pluginVersion + "</h2>");
                printCheck(writer, "Validation check", versionReport.validationCheck);
                printCheck(writer, "Configuration cache", versionReport.configurationCacheCheck);

                for (PluginVersionVerification.GradleVersionCompatibility gradleVersionReport : versionReport.gradleVersionChecks) {
                    writer.println("<h3>Gradle Version: " + gradleVersionReport.gradleVersion + "</h3>");
                    printCheck(writer, "Base compatibility", gradleVersionReport.compatibilityCheck);
                    printCheck(writer, "Incremental build", gradleVersionReport.incrementalBuildCheck);
                    printCheck(writer, "Build cache", gradleVersionReport.buildCacheCheck);
                }
            }
        }

        writer.println("</body></html>");
        writer.close();
    }

    private void printCheck(PrintWriter writer, String title, PluginVersionVerification.VerificationResult validationCheck) {
        if (validationCheck == null) {
            writer.println("<h5>" + title + " - N/A</h5>");
        } else {
            writer.println("<h5>" + title + " - " + (validationCheck.passed ? "OK" : "FAIL") + "</h5>");
            if (validationCheck.buildScanId != null) {
                writer.println("<a href=\"https://gradle.com/s/" + validationCheck.buildScanId + "\"  target=\"_blank\">Build scan</a>");
            }
            writer.println("<pre>" + validationCheck.output + "</pre>");
        }
    }
}
