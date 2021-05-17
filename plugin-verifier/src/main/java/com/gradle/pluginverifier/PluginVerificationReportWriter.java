package com.gradle.pluginverifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class PluginVerificationReportWriter {
    private final PrintWriter resultWriter;

    public PluginVerificationReportWriter(File resultsFile) throws FileNotFoundException {
        this.resultWriter = new PrintWriter(resultsFile);
    }

    public void writeReport(PluginVerificationReport report) {
        writeHeader(report.plugin);
        writeResults(report.pluginValidationCheck);
        writeResults(report.configurationCacheCheck);
        for (PluginVerificationReport.GradleVersionCompatibility compatibility : report.gradleVersionChecks) {
            writeResults(compatibility.compatibilityCheck);
            writeResults(compatibility.incrementalBuildCheck);
            writeResults(compatibility.buildCacheCheck);
        }
    }

    public void close() {
        resultWriter.flush();
        resultWriter.close();
    }

    private void writeHeader(PluginSample plugin) {
        resultWriter.println("=================================");
        resultWriter.println("Plugin verification: " + plugin.getPluginId() + ":" + plugin.getPluginVersion());
        resultWriter.println("=================================");
    }

    private void writeResults(PluginVerificationReport.VerificationResult result) {
        if (result == null) {
            return;
        }
        resultWriter.println("--------------------------");
        resultWriter.println(result.getTitle());
        resultWriter.println(result.getPassed() ? "SUCCESS" : "FAILED");
        resultWriter.println("--------------------------");
        resultWriter.print(result.getOutput());
        resultWriter.println("--------------------------");
        resultWriter.println();
    }

}
