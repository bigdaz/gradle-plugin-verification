package com.gradle.pluginverifier;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
        List<String> gradleVersions = GradleVersions.getAllTested();
        File reportFile = getReportDir().file("index.html").get().getAsFile();
        PrintWriter writer = new PrintWriter(reportFile);
        writer.println("<html>" +
                "<head><style>table, th, td {border: 1px solid black;}</style></head>" +
                "<body>");
        writer.println("<h1>Plugin compatibility pluginReport</h1>");
        writer.print("<table>" +
                "<tr><th>Plugin</th><th>Annotations</th><th>Task Config Avoidance</th><th>Config Cache</th>");
        for (String gradleVersion : gradleVersions) {
            writer.print("<th>Gradle " + gradleVersion + "</th>");
        }
        writer.println("</tr>");

        File[] resultDirs = getResultFiles().get().getAsFile().listFiles(File::isDirectory);
        for (File resultDir : Arrays.stream(resultDirs).sorted().collect(Collectors.toList())) {
            File[] resultFiles = resultDir.listFiles((dir, name) -> name.endsWith(".json"));
            for (File resultFile : resultFiles) {
                PluginVersionVerification versionReport = PluginVersionVerification.fromJson(resultFile);
                File detailedReport = writeDetailedPluginReport(versionReport);
                writer.println("<tr><td><a href=\"" + detailedReport.getName()  + "\">" + versionReport.pluginId + ":" + versionReport.pluginVersion+ "</a></td>" + passed(versionReport.validationCheck) + passed(versionReport.eagerTaskCreationCheck) + passed(versionReport.configurationCacheCheck));
                Map<String, String> gradleVersionSummary = gradleVersionSummary(versionReport.gradleVersionChecks);
                for (String gradleVersion : gradleVersions) {
                    writer.print("<td>" + gradleVersionSummary.get(gradleVersion) + "</td>");
                }
                writer.println("</tr>");
            }
        }
        writer.println("</table>");
        writer.println("</body></html>");
        writer.close();
    }

    private File writeDetailedPluginReport(PluginVersionVerification report) throws FileNotFoundException {
        File reportFile = getReportDir().file(report.pluginId + "_" + report.pluginVersion + ".html").get().getAsFile();
        PrintWriter writer = new PrintWriter(reportFile);
        writer.println("<html><body>");

        writer.println("<h1>Plugin: " + report.pluginId + "</h1>");
        writer.println("<h2 id='" + report.pluginVersion + "'>Version: " + report.pluginVersion + "</h2>");
        printCheck(writer, "Validation check", report.validationCheck);
        printCheck(writer, "Eager task creation check", report.eagerTaskCreationCheck);
        printCheck(writer, "Configuration cache", report.configurationCacheCheck);

        for (PluginVersionVerification.GradleVersionCompatibility gradleVersionReport : report.gradleVersionChecks) {
            writer.println("<h3>Gradle Version: " + gradleVersionReport.gradleVersion + "</h3>");
            printCheck(writer, "Base compatibility", gradleVersionReport.compatibilityCheck);
            printCheck(writer, "Incremental build", gradleVersionReport.incrementalBuildCheck);
            printCheck(writer, "Build cache", gradleVersionReport.buildCacheCheck);
            printCheck(writer, "Build cache relocated", gradleVersionReport.relocatedBuildCacheCheck);
        }

        writer.println("</body></html>");
        writer.close();

        return reportFile;
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
