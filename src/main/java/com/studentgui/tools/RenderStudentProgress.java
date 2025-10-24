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

public class RenderStudentProgress {
    public static void main(String[] args) throws Exception {
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
}
