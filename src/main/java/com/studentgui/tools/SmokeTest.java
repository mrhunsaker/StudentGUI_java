package com.studentgui.tools;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.studentgui.apphelpers.Helpers;
import com.studentgui.apppages.JLineGraph;

/**
 * Minimal automated smoke test for chart rendering and PNG export functionality.
 *
 * <p>Generates deterministic synthetic assessment data, renders it via {@link JLineGraph},
 * and writes a PNG to the app data folder. Used to verify:</p>
 * <ul>
 *   <li>JFreeChart rendering pipeline functions correctly</li>
 *   <li>PNG export via {@link JLineGraph#saveChart} produces valid image files</li>
 *   <li>File I/O permissions and folder creation work as expected</li>
 *   <li>Chart layout and visual appearance match expectations (manual review)</li>
 * </ul>
 *
 * <p><b>Usage:</b></p>
 * <pre>{@code
 * java -cp StudentDataGUI.jar com.studentgui.tools.SmokeTest
 * }</pre>
 *
 * <p><b>Expected Output:</b></p>
 * <pre>
 * Smoke test wrote chart to: /path/to/app_home/StudentDataFiles/Smoke_Test/plots/SmokeTest-2024-01-15.png
 * Exists: true
 * </pre>
 *
 * <p><b>Test Data:</b> Generates 3 synthetic sessions with 28 skills each, using
 * the formula {@code (skillIndex + sessionIndex) % 5} to produce deterministic
 * values in the 0–4 range.</p>
 *
 * <p><b>Output Location:</b> {@code app_home/StudentDataFiles/Smoke_Test/plots/SmokeTest-<ISO_DATE>.png}</p>
 *
 * <p><b>Validation:</b> Success is indicated by "Exists: true" output and a valid
 * 800×400px PNG file at the reported path. Visual inspection of the chart should show
 * 3 line series (2 gray, 1 black) with colored background bands.</p>
 *
 * @see com.studentgui.apppages.JLineGraph#updateWithData
 * @see com.studentgui.apppages.JLineGraph#saveChart
 * @see com.studentgui.apphelpers.Helpers#createFolderHierarchy()
 */
public class SmokeTest {
    /**
     * Entry point for the smoke test.
     *
     * @param args ignored
     * @throws Exception on IO or chart errors
     */
    public static void main(final String[] args) throws Exception {
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
