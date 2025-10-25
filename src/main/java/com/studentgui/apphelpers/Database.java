package com.studentgui.apphelpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralized database helper for the normalized SQLite schema.
 *
 * <p>Provides convenience methods to get-or-create Students and ProgressTypes,
 * create ProgressSessions, ensure AssessmentParts, insert/fetch assessment
 * results, and save session-specific notes. Use these helpers instead of
 * running per-page DDL throughout the codebase.</p>
 */
public class Database {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Database() {
        throw new AssertionError("Database is a utility class");
    }

    /**
     * Obtain a new JDBC Connection to the application SQLite database.
     * Caller is responsible for closing the connection (try-with-resources is recommended).
     *
     * @return new Connection
     * @throws SQLException if the driver cannot open the database
     */
    private static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlite:" + Helpers.DATABASE_PATH.toString();
        return DriverManager.getConnection(url);
    }
    
    /**
     * Get a student id by name, creating a new Student row when none exists.
     *
     * @param name student display name
     * @return id of the existing or newly created student
     * @throws SQLException on database errors
     */
    public static int getOrCreateStudent(String name) throws SQLException {
        try (Connection c = getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM Student WHERE name = ?")) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO Student(name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        }
        throw new SQLException("Failed to create or retrieve student");
    }

    /**
     * Get or create a ProgressType row by name.
     *
     * @param name progress type display name
     * @return database id of the progress type
     * @throws SQLException on database errors
     */
    public static int getOrCreateProgressType(String name) throws SQLException {
        try (Connection c = getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM ProgressType WHERE name = ?")) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO ProgressType(name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        }
        throw new SQLException("Failed to create or retrieve ProgressType");
    }

    /**
     * Ensure AssessmentPart rows exist for the given progress type. This uses
     * SQL "INSERT OR IGNORE" so existing parts are preserved.
     *
     * @param progressTypeId id of the ProgressType
     * @param codes array of part codes to ensure
     * @throws SQLException on database errors
     */
    public static void ensureAssessmentParts(int progressTypeId, String[] codes) throws SQLException {
        try (Connection c = getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("INSERT OR IGNORE INTO AssessmentPart(progress_type_id, code, description) VALUES (?, ?, NULL)")) {
                for (String code : codes) {
                    ps.setInt(1, progressTypeId);
                    ps.setString(2, code);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    /**
     * Remove any AssessmentPart rows for the given progress type whose code is
     * not present in the provided canonical codes array. This helps clean up
     * legacy/malformed entries that could cause part ordering mismatches.
     *
     * @param progressTypeId id of the ProgressType
     * @param allowedCodes canonical set of codes to keep
     * @throws SQLException on database errors
     */
    public static void cleanupAssessmentParts(int progressTypeId, String[] allowedCodes) throws SQLException {
        if (allowedCodes == null || allowedCodes.length == 0) {
            return;
        }
        try (Connection c = getConnection()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < allowedCodes.length; i++) {
                if (i > 0) sb.append(',');
                sb.append('?');
            }
            String sql = "DELETE FROM AssessmentPart WHERE progress_type_id = ? AND code NOT IN (" + sb.toString() + ")";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, progressTypeId);
                for (int i = 0; i < allowedCodes.length; i++) {
                    ps.setString(i + 2, allowedCodes[i]);
                }
                ps.executeUpdate();
            }
        }
    }

    /**
     * Create a ProgressSession for a student and progress type on the given date.
     *
     * @param studentId existing student id
     * @param progressTypeId existing progress type id
     * @param date session date
     * @return generated ProgressSession id
     * @throws SQLException on database errors
     */
    public static int createProgressSession(int studentId, int progressTypeId, LocalDate date) throws SQLException {
        try (Connection c = getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO ProgressSession(student_id, progress_type_id, date, notes) VALUES (?, ?, ?, NULL)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, studentId);
                ps.setInt(2, progressTypeId);
                ps.setString(3, date.toString());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create ProgressSession");
    }

    /**
     * Insert assessment results for a session. The {@code codes} and {@code scores}
     * arrays must be parallel and correspond to existing AssessmentPart codes.
     * Unknown part codes are ignored.
     *
     * @param sessionId progress session id
     * @param progressTypeId progress type id
     * @param codes array of part codes
     * @param scores array of integer scores
     * @throws SQLException on database errors
     */
    public static void insertAssessmentResults(int sessionId, int progressTypeId, String[] codes, int[] scores) throws SQLException {
        if (codes.length != scores.length) throw new IllegalArgumentException("codes and scores length mismatch");
        try (Connection c = getConnection()) {
            // cache part ids
            Map<String, Integer> partIdMap = new HashMap<>();
            try (PreparedStatement ps = c.prepareStatement("SELECT id, code FROM AssessmentPart WHERE progress_type_id = ?")) {
                ps.setInt(1, progressTypeId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        partIdMap.put(rs.getString("code"), rs.getInt("id"));
                    }
                }
            }
            try (PreparedStatement ins = c.prepareStatement("INSERT INTO AssessmentResult(session_id, part_id, score) VALUES (?, ?, ?)") ) {
                for (int i = 0; i < codes.length; i++) {
                    Integer partId = partIdMap.get(codes[i]);
                    if (partId == null) {
                        // skip unknown part
                        continue;
                    }
                    ins.setInt(1, sessionId);
                    ins.setInt(2, partId);
                    ins.setInt(3, scores[i]);
                    ins.addBatch();
                }
                ins.executeBatch();
            }
        }
    }

    /**
     * Fetch the latest assessment result rows for a named student and progress type.
     * Each returned row is a list of integer scores for the parts in canonical
     * part order.
     *
     * @param studentName student display name
     * @param progressTypeName progress type display name
     * @param limit maximum number of recent sessions to fetch
     * @return list of rows, each row is a list of integer scores
     * @throws SQLException on database errors
     */
    public static List<List<Integer>> fetchLatestAssessmentResults(String studentName, String progressTypeName, int limit) throws SQLException {
        List<List<Integer>> result = new ArrayList<>();
        try (Connection c = getConnection()) {
            Integer studentId = null;
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM Student WHERE name = ?")) {
                ps.setString(1, studentName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studentId = rs.getInt(1);
                    }
                }
            }
            if (studentId == null) {
                return result;
            }

            Integer progressTypeId = null;
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM ProgressType WHERE name = ?")) {
                ps.setString(1, progressTypeName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        progressTypeId = rs.getInt(1);
                    }
                }
            }
            if (progressTypeId == null) {
                return result;
            }

            // get parts in canonical order (by id)
            List<Integer> partIds = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement("SELECT id, code FROM AssessmentPart WHERE progress_type_id = ? ORDER BY id ASC")) {
                ps.setInt(1, progressTypeId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        partIds.add(rs.getInt("id"));
                    }
                }
            }

            // get latest session ids for this student and progress type
            List<Integer> sessionIds = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM ProgressSession WHERE student_id = ? AND progress_type_id = ? ORDER BY id DESC LIMIT ?")) {
                ps.setInt(1, studentId);
                ps.setInt(2, progressTypeId);
                ps.setInt(3, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) sessionIds.add(rs.getInt(1));
                }
            }

            // For each session, fetch scores mapped to parts
            for (Integer sid : sessionIds) {
                Map<Integer, Integer> scoreByPart = new HashMap<>();
                try (PreparedStatement ps = c.prepareStatement("SELECT part_id, score FROM AssessmentResult WHERE session_id = ?")) {
                    ps.setInt(1, sid);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            scoreByPart.put(rs.getInt("part_id"), rs.getInt("score"));
                        }
                    }
                }
                List<Integer> row = new ArrayList<>();
                for (Integer pid : partIds) {
                    Integer s = scoreByPart.get(pid);
                    row.add(s == null ? 0 : s);
                }
                result.add(row);
            }
        }
        return result;
    }

    /**
     * Simple holder for the result rows and their corresponding session dates.
     */
    public static class ResultsWithDates {
        public final java.util.List<java.time.LocalDate> dates;
        public final java.util.List<java.util.List<Integer>> rows;
        public ResultsWithDates(java.util.List<java.time.LocalDate> dates, java.util.List<java.util.List<Integer>> rows) {
            this.dates = dates;
            this.rows = rows;
        }
    }

    /**
     * Fetch the latest assessment rows along with their session dates.
     * Rows and dates are ordered oldest-first to facilitate time series plotting.
     */
    public static ResultsWithDates fetchLatestAssessmentResultsWithDates(String studentName, String progressTypeName, int limit) throws SQLException {
        java.util.List<java.util.List<Integer>> result = new ArrayList<>();
        java.util.List<java.time.LocalDate> dates = new ArrayList<>();
        try (Connection c = getConnection()) {
            Integer studentId = null;
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM Student WHERE name = ?")) {
                ps.setString(1, studentName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studentId = rs.getInt(1);
                    }
                }
            }
            if (studentId == null) {
                return new ResultsWithDates(dates, result);
            }

            Integer progressTypeId = null;
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM ProgressType WHERE name = ?")) {
                ps.setString(1, progressTypeName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        progressTypeId = rs.getInt(1);
                    }
                }
            }
            if (progressTypeId == null) {
                return new ResultsWithDates(dates, result);
            }

            // get parts in canonical order (by id)
            java.util.List<Integer> partIds = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement("SELECT id, code FROM AssessmentPart WHERE progress_type_id = ? ORDER BY id ASC")) {
                ps.setInt(1, progressTypeId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        partIds.add(rs.getInt("id"));
                    }
                }
            }

            // get latest session ids and dates for this student and progress type
            java.util.List<java.lang.Integer> sessionIds = new ArrayList<>();
            java.util.List<java.time.LocalDate> sessionDates = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement("SELECT id, date FROM ProgressSession WHERE student_id = ? AND progress_type_id = ? ORDER BY id DESC LIMIT ?")) {
                ps.setInt(1, studentId); ps.setInt(2, progressTypeId); ps.setInt(3, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        sessionIds.add(rs.getInt("id"));
                        sessionDates.add(java.time.LocalDate.parse(rs.getString("date")));
                    }
                }
            }

            // We want chronological order (oldest first)
            java.util.Collections.reverse(sessionIds);
            java.util.Collections.reverse(sessionDates);

            // For each session, fetch scores mapped to parts and append row
            for (Integer sid : sessionIds) {
                Map<Integer, Integer> scoreByPart = new HashMap<>();
                try (PreparedStatement ps = c.prepareStatement("SELECT part_id, score FROM AssessmentResult WHERE session_id = ?")) {
                    ps.setInt(1, sid);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            scoreByPart.put(rs.getInt("part_id"), rs.getInt("score"));
                        }
                    }
                }
                java.util.List<Integer> row = new ArrayList<>();
                for (Integer pid : partIds) {
                    Integer s = scoreByPart.get(pid);
                    row.add(s == null ? 0 : s);
                }
                result.add(row);
            }
            dates.addAll(sessionDates);
        }
        return new ResultsWithDates(dates, result);
    }

    /**
     * Insert a keyboarding-specific result linked to a ProgressSession.
     *
     * @param sessionId existing session id
     * @param program program or curriculum name
     * @param topic topic or lesson name
     * @param speed words-per-minute
     * @param accuracy accuracy percent
     * @throws SQLException on database errors
     */
    public static void insertKeyboardingResult(int sessionId, String program, String topic, int speed, int accuracy) throws SQLException {
        try (Connection c = getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO KeyboardingResult(session_id, program, topic, speed, accuracy) VALUES (?, ?, ?, ?, ?)")) {
                ps.setInt(1, sessionId);
                ps.setString(2, program);
                ps.setString(3, topic);
                ps.setInt(4, speed);
                ps.setInt(5, accuracy);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Save free-form notes for a given ProgressSession.
     *
     * @param sessionId progress session id
     * @param notes free-form notes text
     * @throws SQLException on database errors
     */
    public static void saveSessionNotes(int sessionId, String notes) throws SQLException {
        try (Connection c = getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("UPDATE ProgressSession SET notes = ? WHERE id = ?")) {
                ps.setString(1, notes);
                ps.setInt(2, sessionId);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Save structured contact log details for a given ProgressSession. This
     * will insert or replace a single ContactLog row tied to the session.
     *
     * @param sessionId existing session id
     * @param studentName student display name
     * @param date session date as text
     * @param guardianName guardian or parent name
     * @param contactMethod method of contact (phone/email/etc)
     * @param phoneNumber phone number string
     * @param emailAddress email address string
     * @param contactResponse short description of response
     * @param contactGeneral general contact summary
     * @param contactSpecific specific items discussed
     * @param contactNotes free-form notes
     * @throws SQLException on database errors
     */
    public static void saveContactLog(int sessionId, String studentName, String date, String guardianName, String contactMethod, String phoneNumber, String emailAddress, String contactResponse, String contactGeneral, String contactSpecific, String contactNotes) throws SQLException {
        try (Connection c = getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("INSERT OR REPLACE INTO ContactLog(session_id, student_name, date, guardian_name, contact_method, phone_number, email_address, contact_response, contact_general, contact_specific, contact_notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)") ) {
                ps.setInt(1, sessionId);
                ps.setString(2, studentName);
                ps.setString(3, date);
                ps.setString(4, guardianName);
                ps.setString(5, contactMethod);
                ps.setString(6, phoneNumber);
                ps.setString(7, emailAddress);
                ps.setString(8, contactResponse);
                ps.setString(9, contactGeneral);
                ps.setString(10, contactSpecific);
                ps.setString(11, contactNotes);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Fetch the most recent ContactLog entry for the given student name.
     * Returns a map of column names to string values, or null if none found.
    *
    * @param studentName student display name to search for
    * @return map of contact log columns to values or null when not found
    * @throws SQLException on database errors
     */
    public static com.studentgui.apphelpers.dto.ContactPayload fetchLatestContactLog(String studentName) throws SQLException {
        try (Connection c = getConnection()) {
            Integer studentId = null;
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM Student WHERE name = ?")) {
                ps.setString(1, studentName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studentId = rs.getInt(1);
                    }
                }
            }
            if (studentId == null) {
                return null;
            }

            // Find the latest session id for ProgressType 'ContactLog'
            Integer ptId = null;
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM ProgressType WHERE name = ?")) {
                ps.setString(1, "ContactLog");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ptId = rs.getInt(1);
                    }
                }
            }
            if (ptId == null) {
                return null;
            }

            Integer sessionId = null;
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM ProgressSession WHERE student_id = ? AND progress_type_id = ? ORDER BY id DESC LIMIT 1")) {
                ps.setInt(1, studentId);
                ps.setInt(2, ptId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        sessionId = rs.getInt(1);
                    }
                }
            }
            if (sessionId == null) {
                return null;
            }

            try (PreparedStatement ps = c.prepareStatement("SELECT student_name, date, guardian_name, contact_method, phone_number, email_address, contact_response, contact_general, contact_specific, contact_notes FROM ContactLog WHERE session_id = ? ORDER BY id DESC LIMIT 1")) {
                ps.setInt(1, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        com.studentgui.apphelpers.dto.ContactPayload p = new com.studentgui.apphelpers.dto.ContactPayload(
                            sessionId,
                            rs.getString("guardian_name"),
                            rs.getString("contact_method"),
                            rs.getString("phone_number"),
                            rs.getString("email_address"),
                            rs.getString("contact_response"),
                            rs.getString("contact_general"),
                            rs.getString("contact_specific"),
                            rs.getString("contact_notes")
                        );
                        return p;
                    }
                }
            }
            return null;
        }
    }

}
