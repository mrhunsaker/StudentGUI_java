package com.studentgui.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.studentgui.apphelpers.Database;
import com.studentgui.apphelpers.Helpers;

public class QueryStudentData {
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
}
