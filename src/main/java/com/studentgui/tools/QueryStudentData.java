package com.studentgui.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.studentgui.apphelpers.Database;
import com.studentgui.apphelpers.Helpers;

/**
 * Command-line inspection tool for viewing student database contents and schema statistics.
 *
 * <p>Provides a quick diagnostic view of database state without launching the GUI.
 * Useful for:</p>
 * <ul>
 *   <li>Verifying student records exist in the database</li>
 *   <li>Inspecting available progress types and their assessment part counts</li>
 *   <li>Checking session data row sizes for debugging schema migrations</li>
 *   <li>Quick manual data verification during development or troubleshooting</li>
 * </ul>
 *
 * <p><b>Usage:</b></p>
 * <pre>{@code
 * # List all students and progress types with counts
 * java -cp StudentDataGUI.jar com.studentgui.tools.QueryStudentData
 *
 * # Inspect specific student's progress types
 * java -cp StudentDataGUI.jar com.studentgui.tools.QueryStudentData "Aaron A Aaronsson"
 * }</pre>
 *
 * <p><b>Output Format:</b></p>
 * <pre>
 * Inspecting student: Aaron A Aaronsson
 * ProgressType 'Braille' (id=1) parts=64 sessions=3
 *  Sample row sizes: 64 values: [2, 3, 2, 3, 4, ...]
 * ProgressType 'Abacus' (id=2) parts=22 sessions=1
 *  Sample row sizes: 22 values: [0, 1, 2, 1, 3, ...]
 * </pre>
 *
 * <p><b>Workflow:</b></p>
 * <ol>
 *   <li>Lists all known students via {@link Helpers#getStudents()}</li>
 *   <li>Selects first student or uses command-line argument</li>
 *   <li>Queries {@code ProgressType} table for all progress types</li>
 *   <li>For each progress type: counts assessment parts and fetches sample session rows</li>
 *   <li>Prints progress type name, ID, part count, session count, and sample row to stdout</li>
 * </ol>
 *
 * @see com.studentgui.apphelpers.Database#fetchLatestAssessmentResults
 * @see com.studentgui.apphelpers.Helpers#getStudents()
 */
public class QueryStudentData {
    /**
     * Command-line entry point. Prints progress types and a sample row for
     * the specified or first-known student.
     *
     * @param args optional first argument is student display name
     * @throws Exception on database errors
     */
    public static void main(final String[] args) throws Exception {
        Helpers.createFolderHierarchy();
        List<String> students = Helpers.getStudents();
        String student = null;
        if (args.length > 0) {
            student = args[0];
        }
        if (student == null) {
            System.out.println("Known students:");
            for (String s : students) {
                System.out.println(" - " + s);
            }
            if (!students.isEmpty()) {
                student = students.get(0);
            } else {
                System.out.println("No students found in DB. Exiting.");
                return;
            }
        }
        System.out.println("Inspecting student: " + student);
        // list progress types
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + Helpers.DATABASE_PATH.toString())) {
            try (PreparedStatement ps = c.prepareStatement("SELECT id, name FROM ProgressType")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int ptId = rs.getInt("id");
                        String ptName = rs.getString("name");
                        // count parts
                            int partCount = 0;
                        try (PreparedStatement ps2 = c.prepareStatement("SELECT COUNT(*) FROM AssessmentPart WHERE progress_type_id = ?")) {
                            ps2.setInt(1, ptId);
                            try (ResultSet rs2 = ps2.executeQuery()) {
                                if (rs2.next()) {
                                    partCount = rs2.getInt(1);
                                }
                            }
                        }
                        List<List<Integer>> rows = Database.fetchLatestAssessmentResults(student, ptName, 5);
                        System.out.println(String.format("ProgressType '%s' (id=%d) parts=%d sessions=%d", ptName, ptId, partCount, rows.size()));
                        if (!rows.isEmpty()) {
                            System.out.println(" Sample row sizes: " + rows.get(0).size() + " values: " + rows.get(0));
                        }
                    }
                }
            }
        }
    }
    /**
     * No-op public constructor to document this class as a small utility.
     */
    public QueryStudentData() {
        // utility class; no state
    }
}
