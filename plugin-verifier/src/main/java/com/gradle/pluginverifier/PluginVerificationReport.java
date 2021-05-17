package com.gradle.pluginverifier;

import java.io.PrintWriter;

public class PluginVerificationReport {
    private final PrintWriter resultWriter;

    public PluginVerificationReport(PrintWriter resultWriter) {
        this.resultWriter = resultWriter;
    }

    public void writeHeader(PluginSample plugin) {
        resultWriter.println("=================================");
        resultWriter.println("Plugin verification: " + plugin.getPluginId() + ":" + plugin.getPluginVersion());
        resultWriter.println("=================================");
    }

    public void writeResults(String title, boolean passed, String output) {
        resultWriter.println("--------------------------");
        resultWriter.println(title);
        resultWriter.println(passed ? "SUCCESS" : "FAILED");
        resultWriter.println("--------------------------");
        resultWriter.print(output);
        resultWriter.println("--------------------------");
        resultWriter.println();
    }

}
