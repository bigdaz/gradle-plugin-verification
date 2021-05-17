package com.gradle.pluginverifier;

import java.io.PrintWriter;

public class PluginVerificationReportWriter {
    private final PrintWriter resultWriter;

    public PluginVerificationReportWriter(PrintWriter resultWriter) {
        this.resultWriter = resultWriter;
    }

    public void writeHeader(PluginSample plugin) {
        resultWriter.println("=================================");
        resultWriter.println("Plugin verification: " + plugin.getPluginId() + ":" + plugin.getPluginVersion());
        resultWriter.println("=================================");
    }

    public void writeResults(PluginVerificationResult result) {
        resultWriter.println("--------------------------");
        resultWriter.println(result.getTitle());
        resultWriter.println(result.getPassed() ? "SUCCESS" : "FAILED");
        resultWriter.println("--------------------------");
        resultWriter.print(result.getOutput());
        resultWriter.println("--------------------------");
        resultWriter.println();
    }

}
