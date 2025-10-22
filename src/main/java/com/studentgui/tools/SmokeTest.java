package com.studentgui.tools;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.studentgui.apphelpers.Helpers;
import com.studentgui.apppages.JLineGraph;

/**
 * Minimal smoke test to exercise the chart rendering and file export.
 * <p>
 * Generates deterministic sample data, renders it via {@code JLineGraph}
 * and writes a PNG under the app_home plots directory.
 * </p>
 */
public class SmokeTest {
    /**
     * Entry point for the smoke test.
     *
     * @param args ignored
     * @throws Exception on IO or chart errors
     */
    public static void main(String[] args) throws Exception {
        Helpers.createFolderHierarchy();
        JLineGraph graph = new JLineGraph();

        // Create sample data: 3 sessions, each with 28 skill values (0-4)
        List<List<Integer>> data = new ArrayList<>();
        for (int s = 0; s < 3; s++) {
            List<Integer> row = new ArrayList<>();
            for (int i = 0; i < 28; i++) {
                row.add((i + s) % 5); // deterministic sample
            }
            data.add(row);
        }
        graph.updateWithData(data);

        Path outDir = Helpers.APP_HOME.resolve("StudentDataFiles").resolve(Helpers.safeName("Smoke Test")).resolve("plots");
        DateTimeFormatter df = DateTimeFormatter.ISO_DATE;
        Path outFile = outDir.resolve("SmokeTest-" + LocalDate.now().format(df) + ".png");
        graph.saveChart(outFile, 800, 400);
        System.out.println("Smoke test wrote chart to: " + outFile.toAbsolutePath());
        System.out.println("Exists: " + java.nio.file.Files.exists(outFile));
    }

    /**
     * Private constructor to prevent instantiation of this utility test class.
     */
    private SmokeTest() {
        // no instances
    }
}
