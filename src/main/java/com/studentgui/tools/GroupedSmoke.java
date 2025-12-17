package com.studentgui.tools;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.studentgui.apphelpers.Helpers;
import com.studentgui.apppages.JLineGraph;

/**
 * Automated smoke test for grouped chart rendering and multi-panel PNG export.
 *
 * <p>Verifies that {@link JLineGraph} correctly renders multiple stacked phase-grouped
 * charts (as used by assessment pages like Braille, Abacus, etc.). Generates synthetic
 * data with explicit phase prefixes (P1, P2, P3) and exports to PNG.</p>
 *
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Validates phase grouping logic in {@link JLineGraph#updateWithGroupedData}</li>
 *   <li>Ensures each group renders as a separate stacked chart panel</li>
 *   <li>Verifies PNG export of multi-chart layouts</li>
 *   <li>Provides visual reference for chart appearance during development</li>
 * </ul>
 *
 * <p><b>Usage:</b></p>
 * <pre>{@code
 * java -cp StudentDataGUI.jar com.studentgui.tools.GroupedSmoke
 * }</pre>
 *
 * <p><b>Expected Output:</b></p>
 * <pre>
 * Grouped smoke wrote chart to: /path/to/app_home/StudentDataFiles/Grouped_Smoke/plots/GroupedSmoke-2024-01-15.png
 * Exists: true
 * </pre>
 *
 * <p><b>Test Data Structure:</b></p>
 * <ul>
 *   <li><b>Part codes:</b> 9 codes with prefixes: P1 (3 items), P2 (2 items), P3 (4 items)</li>
 *   <li><b>Sessions:</b> 3 synthetic sessions with deterministic scores {@code (i + s) % 5}</li>
 *   <li><b>Expected output:</b> 3 stacked chart panels (one per phase group) in a single 800×600px PNG</li>
 * </ul>
 *
 * <p><b>Output Location:</b> {@code app_home/StudentDataFiles/Grouped_Smoke/plots/GroupedSmoke-<ISO_DATE>.png}</p>
 *
 * <p><b>Validation:</b> Inspect the generated PNG to verify:</p>
 * <ol>
 *   <li>Three distinct chart panels labeled "P1 - 3 items", "P2 - 2 items", "P3 - 4 items"</li>
 *   <li>Each panel shows 3 line series (2 gray historical, 1 black latest)</li>
 *   <li>Colored background bands visible in all panels</li>
 * </ol>
 *
 * @see com.studentgui.apppages.JLineGraph#updateWithGroupedData
 * @see com.studentgui.apppages.JLineGraph#saveChart
 */
public class GroupedSmoke {
    /**
     * Entry point for the grouped smoke utility.
     *
     * @param args ignored
     * @throws Exception on unexpected IO or charting errors
     */
    public static void main(final String[] args) throws Exception {
        Helpers.createFolderHierarchy();
        JLineGraph graph = new JLineGraph();

        // build part codes with P1_, P2_, P3_ groups (3+2+4 items)
        String[] codes = new String[]{"P1_1","P1_2","P1_3","P2_1","P2_2","P3_1","P3_2","P3_3","P3_4"};

        // Create sample data: 3 sessions
        List<List<Integer>> data = new ArrayList<>();
        for (int s = 0; s < 3; s++) {
            List<Integer> row = new ArrayList<>();
            for (int i = 0; i < codes.length; i++) {
                row.add((i + s) % 5);
            }
            data.add(row);
        }
        graph.updateWithGroupedData(data, codes);

        Path outDir = Helpers.APP_HOME.resolve("StudentDataFiles").resolve(Helpers.safeName("Grouped Smoke")).resolve("plots");
        java.nio.file.Files.createDirectories(outDir);
        DateTimeFormatter df = DateTimeFormatter.ISO_DATE;
        Path outFile = outDir.resolve("GroupedSmoke-" + LocalDate.now().format(df) + ".png");
        graph.saveChart(outFile, 800, 600);
        System.out.println("Grouped smoke wrote chart to: " + outFile.toAbsolutePath());
        System.out.println("Exists: " + java.nio.file.Files.exists(outFile));
    }
    /**
     * Public no-arg constructor to document the utility nature of this class.
     * Kept for completeness; all work is performed from {@link #main(String[])}.
     */
    public GroupedSmoke() {
        // no state
    }
}
