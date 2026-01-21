package com.studentgui.test;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.studentgui.apphelpers.Database;
import com.studentgui.apphelpers.Helpers;
import com.studentgui.apppages.Braille;
import com.studentgui.apppages.JLineGraph;

/**
 * Test that generates example Braille exports (per-phase PNGs + MD/HTML)
 * for the student "Test Student". This mirrors the export logic used in
 * the Braille page submit handler but runs headlessly as a test so the
 * agent can produce example files for review.
 */
public class ExportBrailleReportsTest {

    @Test
    /**
     * generateBrailleExport - TODO: describe this method
     */

    public void generateBrailleExport() throws Exception {
        // Force headless mode for chart rendering in CI-like environments
        System.setProperty("java.awt.headless", "true");

        Helpers.createFolderHierarchy();
        // Ensure DB exists and schema is initialized (idempotent)
        com.studentgui.apphelpers.SqlGenerate.initializeDatabase();

        String student = "Test Student";
        String progressType = "Braille";

        // Instantiate the Braille page to ensure canonical parts are created
        JLineGraph graph = new JLineGraph();
        Braille braille = new Braille(student, LocalDate.now(), graph);

        // Fetch historical rows + dates
        Database.ResultsWithDates rwd = Database.fetchLatestAssessmentResultsWithDates(student, progressType, Integer.MAX_VALUE);

        // Reflectively obtain the partCodes and human labels from Braille instance
        Field pcField = Braille.class.getDeclaredField("partCodes");
        pcField.setAccessible(true);
        String[] partCodes = (String[]) pcField.get(braille);

        Field partsField = Braille.class.getDeclaredField("parts");
        partsField.setAccessible(true);
        String[][] parts = (String[][]) partsField.get(braille);
        String[] labels = new String[parts.length];
        for (int i = 0; i < parts.length; i++) labels[i] = parts[i][1];

        java.nio.file.Path out = Helpers.APP_HOME.resolve("StudentDataFiles").resolve(Helpers.safeName(student)).resolve("plots");
        java.nio.file.Files.createDirectories(out);
        String baseName = "Braille-example-" + java.time.LocalDate.now().toString();

        if (rwd != null && rwd.rows != null && !rwd.rows.isEmpty()) {
            graph.updateWithGroupedDataByDate(rwd.dates, rwd.rows, partCodes, labels);
        } else {
            // No historical data; create a single-row from zeros by reflection of Braille's fields
            java.util.List<java.util.List<Integer>> rowsList = new java.util.ArrayList<>();
            java.util.List<Integer> latest = new java.util.ArrayList<>();
            for (int i = 0; i < partCodes.length; i++) latest.add(0);
            rowsList.add(latest);
            graph.updateWithGroupedData(rowsList, partCodes);
        }

        Map<String, Path> groups = graph.saveGroupedCharts(out, baseName, 1000, 240);

        // Build simple markdown and html reports (reuse palette from JLineGraph)
        StringBuilder md = new StringBuilder();
        md.append("# ").append(student).append(" - ").append(java.time.LocalDate.now().toString()).append("\n\n");
        for (Map.Entry<String, Path> e : groups.entrySet()) {
            md.append("## ").append(e.getKey()).append("\n\n");
            md.append("![](./").append(e.getValue().getFileName().toString()).append(")\n\n");
        }
        java.nio.file.Path mdFile = out.resolve(baseName + ".md");
        java.nio.file.Files.writeString(mdFile, md.toString(), java.nio.charset.StandardCharsets.UTF_8);

        String[] palette = JLineGraph.PALETTE_HEX;
        java.util.LinkedHashMap<String, java.util.List<Integer>> groupsIdx = new java.util.LinkedHashMap<>();
        for (int i = 0; i < partCodes.length; i++) {
            String code = partCodes[i];
            String grp = code != null && code.contains("_") ? code.split("_")[0] : code;
            groupsIdx.computeIfAbsent(grp, k -> new java.util.ArrayList<>()).add(i);
        }

        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html><head><meta charset=\"utf-8\"><title>");
        html.append(student).append(" - ").append(java.time.LocalDate.now().toString()).append("</title>");
        html.append("<style>body{font-family:sans-serif;margin:20px;} img{max-width:100%;height:auto;border:1px solid #ccc;margin-bottom:8px;} .legend{max-height:160px;overflow:auto;border:1px solid #ddd;padding:8px;margin-bottom:24px;} .legend-item{display:flex;align-items:center;gap:8px;padding:4px 0;} .swatch{width:18px;height:12px;border:1px solid #333;display:inline-block}</style>");
        html.append("</head><body>");
        html.append("<h1>").append(student).append(" - ").append(java.time.LocalDate.now().toString()).append("</h1>");
        for (Map.Entry<String, Path> e2 : groups.entrySet()) {
            String grp = e2.getKey();
            String imgName = e2.getValue().getFileName().toString();
            html.append("<h2>").append(grp).append("</h2>");
            html.append("<div class=\"plot\"><img src=\"./").append(imgName).append("\" alt=\"").append(grp).append("\"></div>");
            java.util.List<Integer> idxs = groupsIdx.getOrDefault(grp, new java.util.ArrayList<>());
            html.append("<div class=\"legend\">");
            for (int s = 0; s < idxs.size(); s++) {
                int idx = idxs.get(s);
                String code = partCodes[idx];
                String human = labels[idx];
                String seriesName = code + " - " + human;
                String color = palette[s % palette.length];
                html.append("<div class=\"legend-item\">");
                html.append("<span class=\"swatch\" style=\"background:");
                html.append(color).append(";\"></span>");
                html.append("<div>").append(seriesName).append("</div></div>");
            }
            html.append("</div>");
        }
        html.append("</body></html>");
        java.nio.file.Path htmlFile = out.resolve(baseName + ".html");
        java.nio.file.Files.writeString(htmlFile, html.toString(), java.nio.charset.StandardCharsets.UTF_8);

        System.out.println("Exported Braille report to: " + out.toAbsolutePath().toString());
        for (Map.Entry<String, Path> e : groups.entrySet()) System.out.println(" - " + e.getKey() + " -> " + e.getValue().getFileName());
        System.out.println("MD: " + mdFile.getFileName() + " HTML: " + htmlFile.getFileName());

        // Quick assertion to ensure at least one image or the md file exists
        assertTrue(java.nio.file.Files.exists(mdFile));
    }
}
