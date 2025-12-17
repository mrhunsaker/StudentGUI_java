package com.studentgui.tools;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.studentgui.apphelpers.Database;
import com.studentgui.apphelpers.Helpers;
import com.studentgui.apppages.JLineGraph;

/**
 * Command-line utility for offline student progress chart rendering and export.
 *
 * <p>This standalone tool generates PNG charts for a specific student and progress type
 * without launching the full GUI application. Useful for:</p>
 * <ul>
 *   <li>Batch chart generation for multiple students/progress types</li>
 *   <li>Debugging chart rendering issues outside the GUI context</li>
 *   <li>Automated report generation in CI/CD pipelines</li>
 *   <li>Creating historical chart snapshots for archival purposes</li>
 * </ul>
 *
 * <p><b>Usage:</b></p>
 * <pre>{@code
 * java -cp StudentDataGUI.jar com.studentgui.tools.RenderStudentProgress "Aaron A Aaronsson" "Braille"
 * }</pre>
 *
 * <p><b>Workflow:</b></p>
 * <ol>
 *   <li>Ensures app folder hierarchy exists via {@link Helpers#createFolderHierarchy()}</li>
 *   <li>Queries database for canonical assessment part codes for the specified progress type</li>
 *   <li>Fetches up to 5 most recent assessment sessions via {@link Database#fetchLatestAssessmentResults}</li>
 *   <li>Renders grouped chart using {@link JLineGraph#updateWithGroupedData}</li>
 *   <li>Exports PNG to {@code StudentDataFiles/<student>/plots/<ProgressType>-render-<date>.png}</li>
 * </ol>
 *
 * <p><b>Output:</b> PNG file written to student's plots directory with filename format:
 * {@code <ProgressType>-render-<ISO_DATE>.png}</p>
 *
 * @see com.studentgui.apphelpers.Database#fetchLatestAssessmentResults
 * @see com.studentgui.apppages.JLineGraph
 * @see com.studentgui.apphelpers.Helpers#createFolderHierarchy()
 */
public class RenderStudentProgress {
    /**
     * Render and write a progress chart for the provided student and progress type.
     *
     * @param args first arg: student display name, second arg: progress type name
     * @throws Exception on I/O or database access errors
     */
    public static void main(final String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: RenderStudentProgress <Student Name> <ProgressTypeName>");
            return;
        }
        String student = args[0];
        String pt = args[1];
        Helpers.createFolderHierarchy();
        System.out.println("Rendering " + pt + " for " + student);

        // fetch canonical part codes for progress type
        List<String> codes = new ArrayList<>();
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + Helpers.DATABASE_PATH.toString())) {
            try (PreparedStatement ps = c.prepareStatement("SELECT code FROM AssessmentPart ap JOIN ProgressType pt ON ap.progress_type_id = pt.id WHERE pt.name = ? ORDER BY ap.id ASC")) {
                ps.setString(1, pt);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) codes.add(rs.getString(1));
                }
            }
        }
        if (codes.isEmpty()) {
            System.out.println("No parts found for progress type: " + pt);
            return;
        }
        String[] codeArr = codes.toArray(new String[0]);
        List<List<Integer>> rows = Database.fetchLatestAssessmentResults(student, pt, 5);
        if (rows == null || rows.isEmpty()) {
            System.out.println("No session rows for student/progress: " + student + "/" + pt);
            return;
        }
        JLineGraph g = new JLineGraph();
        g.updateWithGroupedData(rows, codeArr);
        Path out = Helpers.APP_HOME.resolve("StudentDataFiles").resolve(Helpers.safeName(student)).resolve("plots");
        java.nio.file.Files.createDirectories(out);
        DateTimeFormatter df = DateTimeFormatter.ISO_DATE;
        Path file = out.resolve(pt + "-render-" + LocalDate.now().format(df) + ".png");
        g.saveChart(file, 1000, 800);
        System.out.println("Wrote: " + file.toAbsolutePath());
    }
    /**
     * Explicit no-arg constructor with documentation to avoid default-constructor javadoc warnings.
     */
    public RenderStudentProgress() {
        // utility
    }
}
