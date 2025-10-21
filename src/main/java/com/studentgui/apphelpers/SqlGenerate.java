package com.studentgui.apphelpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SQL schema generator for the normalized application database.
 *
 * This class ensures the SQLite database file exists and creates the
 * canonical tables used by the application. Safe to call repeatedly on
 * application startup.
 */
/**
 * Utility responsible for creating/validating the on-disk SQLite database
 * and canonical schema used by the application. Safe to call multiple times.
 */
public class SqlGenerate {
    private static final Path DB = Helpers.DATABASE_PATH;
    private static final Logger LOG = LoggerFactory.getLogger(SqlGenerate.class);
    // Ported schema from Python appHelpers/sqlgenerate.py
    private static final String[] SCHEMA = new String[] {
        // Core student table
        """
        CREATE TABLE IF NOT EXISTS Student (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            birthdate TEXT,
            notes TEXT
        );
        """,
        // ProgressType
        """
        CREATE TABLE IF NOT EXISTS ProgressType (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL UNIQUE,
            description TEXT
        );
        """,
        // ProgressSession
        """
        CREATE TABLE IF NOT EXISTS ProgressSession (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            student_id INTEGER NOT NULL,
            progress_type_id INTEGER NOT NULL,
            date TEXT NOT NULL,
            notes TEXT,
            FOREIGN KEY(student_id) REFERENCES Student(id) ON DELETE CASCADE,
            FOREIGN KEY(progress_type_id) REFERENCES ProgressType(id) ON DELETE CASCADE
        );
        """,
        // KeyboardingResult
        """
        CREATE TABLE IF NOT EXISTS KeyboardingResult (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            session_id INTEGER NOT NULL,
            program TEXT NOT NULL,
            topic TEXT NOT NULL,
            speed INTEGER NOT NULL,
            accuracy INTEGER NOT NULL,
            FOREIGN KEY(session_id) REFERENCES ProgressSession(id) ON DELETE CASCADE
        );
        """,
        // TrialResult
        """
        CREATE TABLE IF NOT EXISTS TrialResult (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            session_id INTEGER NOT NULL,
            task TEXT NOT NULL,
            lesson TEXT,
            session_label TEXT,
            trial_number INTEGER NOT NULL,
            score INTEGER,
            FOREIGN KEY(session_id) REFERENCES ProgressSession(id) ON DELETE CASCADE
        );
        """,
        // TrialSessionSummary
        """
        CREATE TABLE IF NOT EXISTS TrialSessionSummary (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            session_id INTEGER NOT NULL UNIQUE,
            median FLOAT,
            notes TEXT,
            FOREIGN KEY(session_id) REFERENCES ProgressSession(id) ON DELETE CASCADE
        );
        """,
        // AssessmentPart
        """
        CREATE TABLE IF NOT EXISTS AssessmentPart (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            progress_type_id INTEGER NOT NULL,
            code TEXT NOT NULL,
            description TEXT,
            UNIQUE(progress_type_id, code),
            FOREIGN KEY(progress_type_id) REFERENCES ProgressType(id) ON DELETE CASCADE
        );
        """,
        // AssessmentResult
        """
        CREATE TABLE IF NOT EXISTS AssessmentResult (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            session_id INTEGER NOT NULL,
            part_id INTEGER NOT NULL,
            score INTEGER,
            FOREIGN KEY(session_id) REFERENCES ProgressSession(id) ON DELETE CASCADE,
            FOREIGN KEY(part_id) REFERENCES AssessmentPart(id) ON DELETE CASCADE
        );
        """
        ,
        // ContactLog details tied to a ProgressSession
        """
        CREATE TABLE IF NOT EXISTS ContactLog (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            session_id INTEGER NOT NULL,
            student_name TEXT,
            date TEXT,
            guardian_name TEXT,
            contact_method TEXT,
            phone_number TEXT,
            email_address TEXT,
            contact_response TEXT,
            contact_general TEXT,
            contact_specific TEXT,
            contact_notes TEXT,
            FOREIGN KEY(session_id) REFERENCES ProgressSession(id) ON DELETE CASCADE
        );
        """
    };

    /**
     * Ensure the database file and canonical schema exist. This method is idempotent
     * and safe to call on application startup.
     */
    /**
     * Ensure the database file and canonical schema exist. This method is idempotent
     * and safe to call on application startup.
     */
    public static void initializeDatabase() {
        try {
            Path parent = DB.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            if (Files.exists(DB) && Files.isDirectory(DB)) {
                LOG.error("Path is a directory, cannot create DB file: {}", DB);
                return;
            }
            if (Files.exists(DB)) {
                LOG.info("Database already exists at {}", DB);
                // even if the DB exists, ensure schema is present by connecting and executing schema statements
            }
            // create/connect to SQLite database file by opening a connection
            String url = "jdbc:sqlite:" + DB.toString();
            try (Connection conn = DriverManager.getConnection(url)) {
                if (conn != null) {
                    executeSchema(conn);
                }
            }
            LOG.info("Database initialized/validated at {}", DB);
        } catch (SQLException | IOException e) {
            LOG.error("Error initializing database", e);
        }
    }

    /**
     * Execute the SCHEMA statements on the provided connection.
     * Extracted to make the schema application clearer and easier to test.
     */
    private static void executeSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            for (String sql : SCHEMA) {
                st.execute(sql);
            }
        }
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private SqlGenerate() {
        throw new AssertionError("Not instantiable");
    }
}
