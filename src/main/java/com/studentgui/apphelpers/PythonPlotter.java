package com.studentgui.apphelpers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to invoke the repository's Python plot runner asynchronously.
 */
public class PythonPlotter {
    private static final Logger LOG = LoggerFactory.getLogger(PythonPlotter.class);

    /**
     * Run the python runner for the given module and student name in a background thread.
     * The onComplete consumer receives combined stdout/stderr text when the process finishes.
     */
    public static void runPlotAsync(String moduleName, String studentName, Consumer<String> onComplete) {
        if (studentName == null || studentName.trim().isEmpty()) {
            String msg = "No student selected for plot generation";
            LOG.warn(msg);
            if (onComplete != null) onComplete.accept(msg);
            return;
        }

        Path script = Helpers.PROJECT_ROOT.resolve("appPages").resolve("run_plot.py");

        Thread t = new Thread(() -> {
            StringBuilder out = new StringBuilder();
            try {
                ProcessBuilder pb = new ProcessBuilder("python", script.toString(), moduleName, studentName);
                pb.directory(Helpers.PROJECT_ROOT.toFile());
                pb.redirectErrorStream(true);
                Process p = pb.start();
                try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        out.append(line).append(System.lineSeparator());
                    }
                }
                int rc = p.waitFor();
                out.append("Exit code: ").append(rc).append(System.lineSeparator());
            } catch (java.io.IOException | InterruptedException e) {
                LOG.error("Error running python plot runner", e);
                out.append("Error: ").append(e.toString()).append(System.lineSeparator());
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            }
            if (onComplete != null) {
                try {
                    onComplete.accept(out.toString());
                } catch (RuntimeException ex) {
                    LOG.warn("onComplete consumer threw", ex);
                }
            }
        }, "PythonPlotter-" + moduleName);
        t.setDaemon(true);
        t.start();
    }
}
