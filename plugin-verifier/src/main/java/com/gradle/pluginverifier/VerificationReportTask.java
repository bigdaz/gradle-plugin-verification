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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class VerificationReportTask extends DefaultTask {
    @InputDirectory
    public abstract DirectoryProperty getResultFiles();

    @OutputDirectory
    public abstract DirectoryProperty getReportDir();

    @TaskAction
    public void generate() throws FileNotFoundException {
        File reportFile = getReportDir().file("index.html").get().getAsFile();
        PrintWriter writer = new PrintWriter(reportFile);
        writer.println("<html>" +
                "<head><style>table, th, td {border: 1px solid black;}</style></head>" +
                "<body>");
        writer.println("<h1>Plugin compatibility report</h1>");
        writer.println("<table>" +
                "<tr><th>Plugin</th><th>Annotations</th><th>Task Config Avoidance</th><th>Config Cache</th><th>Gradle 7.0.2</th><th>Gradle 6.8.3</th><th>gradle 5.6.4</th></tr>");

        File[] resultFiles = getResultFiles().get().getAsFile().listFiles();
        for (File resultFile : Arrays.stream(resultFiles).sorted().collect(Collectors.toList())) {
            PluginVerificationReport pluginReport = new Gson().fromJson(Files.newReader(resultFile, Charset.defaultCharset()), PluginVerificationReport.class);
            writeDetailedPluginReport(pluginReport);
            for (PluginVersionVerification versionReport : pluginReport.pluginVersions) {
                writer.println("<tr><td><a href=\"" + pluginReport.pluginId + ".html#" + versionReport.pluginVersion + "\">" + pluginReport.pluginId + ":" + versionReport.pluginVersion+ "</a></td>" + passed(versionReport.validationCheck) + passed(versionReport.eagerTaskCreationCheck) + passed(versionReport.configurationCacheCheck));
                Map<String, String> gradleVersionSummary = gradleVersionSummary(versionReport.gradleVersionChecks);
                writer.println("<td>" + gradleVersionSummary.get("7.0.2") + "</td><td>" + gradleVersionSummary.get("6.8.3") + "</td><td>" + gradleVersionSummary.get("5.6.4") + "</td></tr>");
            }
        }
        writer.println("</table>");
        writer.println("</body></html>");
        writer.close();
    }

    private void writeDetailedPluginReport(PluginVerificationReport pluginReport) throws FileNotFoundException {
        File reportFile = getReportDir().file(pluginReport.pluginId + ".html").get().getAsFile();
        PrintWriter writer = new PrintWriter(reportFile);
        writer.println("<html><body>");

        writer.println("<h1>Plugin: " + pluginReport.pluginId + "</h1>");
        for (PluginVersionVerification versionReport : pluginReport.pluginVersions) {
            writer.println("<h2 id='" + versionReport.pluginVersion + "'>Version: " + versionReport.pluginVersion + "</h2>");
            printCheck(writer, "Validation check", versionReport.validationCheck);
            printCheck(writer, "Eager task creation check", versionReport.eagerTaskCreationCheck);
            printCheck(writer, "Configuration cache", versionReport.configurationCacheCheck);

            for (PluginVersionVerification.GradleVersionCompatibility gradleVersionReport : versionReport.gradleVersionChecks) {
                writer.println("<h3>Gradle Version: " + gradleVersionReport.gradleVersion + "</h3>");
                printCheck(writer, "Base compatibility", gradleVersionReport.compatibilityCheck);
                printCheck(writer, "Incremental build", gradleVersionReport.incrementalBuildCheck);
                printCheck(writer, "Build cache", gradleVersionReport.buildCacheCheck);
                printCheck(writer, "Build cache relocated", gradleVersionReport.relocatedBuildCacheCheck);
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

    private String passed(PluginVersionVerification.VerificationResult result) {
        return result.passed ? "<td>OK</td>" : "<td>FAIL</td>";
    }

    private Map<String, String> gradleVersionSummary(List<PluginVersionVerification.GradleVersionCompatibility> gradleVersionChecks) {
        Map<String, String> map = new LinkedHashMap<>();
        for (PluginVersionVerification.GradleVersionCompatibility gradleVersionCheck : gradleVersionChecks) {
            map.put(gradleVersionCheck.gradleVersion, describeCompatibility(gradleVersionCheck));
        }
        return map;
    }

    private String describeCompatibility(PluginVersionVerification.GradleVersionCompatibility gradleVersionCompatibility) {
        if (gradleVersionCompatibility.compatibilityCheck == null || !gradleVersionCompatibility.compatibilityCheck.passed) {
            return "FAIL";
        }
        if (gradleVersionCompatibility.incrementalBuildCheck == null) {
            // Not incremental
            return "OK";
        }
        if (!gradleVersionCompatibility.incrementalBuildCheck.passed) {
            return "NOT UP-TO-DATE";
        }
        if (gradleVersionCompatibility.buildCacheCheck == null || !gradleVersionCompatibility.buildCacheCheck.passed) {
            return "UP-TO-DATE";
        }
        if (gradleVersionCompatibility.relocatedBuildCacheCheck == null || !gradleVersionCompatibility.relocatedBuildCacheCheck.passed) {
            return "CACHED (relative)";
        }
        return "CACHED";
    }
}
