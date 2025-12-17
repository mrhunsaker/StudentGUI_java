package com.studentgui.apphelpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Miscellaneous filesystem and small utility helpers used by the UI pages.
 *
 * Responsibilities include selecting and creating the application home
 * directory, creating per-student folder hierarchies, and providing a
 * small roster fallback when no students.json exists.
 */
public class Helpers {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Helpers() {
        throw new AssertionError("Helpers is a utility class");
    }
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Helpers.class);
    /** The project working directory (where the process was started). */
    public static final Path PROJECT_ROOT = Paths.get(System.getProperty("user.dir"));
    /** Application home used for storing app-specific files (defaults to ./app_home). */
    public static final Path APP_HOME = selectAppHome();
    /** Root directory for persisted application data (alias of APP_HOME). */
    public static final Path DATA_ROOT = APP_HOME;
    /** Directory that holds the database file. */
    public static final Path DATABASE_ROOT = DATA_ROOT.resolve("StudentDatabase");
    /** Canonical database file path used by SQLite operations. */
    public static final Path DATABASE_PATH = DATABASE_ROOT.resolve("students20252026.db");

    /**
     * Select a suitable application home directory. Attempts to use a
     * ./app_home subdirectory of the working directory and falls back to the
     * system temporary directory if creation fails.
     */
    private static Path selectAppHome() {
        try {
            Path candidate = PROJECT_ROOT.resolve("app_home");
            Files.createDirectories(candidate);
            // test write
            Path test = candidate.resolve(".write_test");
            Files.writeString(test, "");
            Files.deleteIfExists(test);
            return candidate;
        } catch (IOException e) {
                LOG.debug("Unable to create app_home; falling back to temp dir", e);
                try {
                    Path tmp = Paths.get(System.getProperty("java.io.tmpdir"), "StudentDataGUI");
                    Files.createDirectories(tmp);
                    return tmp;
                } catch (IOException ex) {
                    LOG.debug("Unable to create fallback temp dir; using CWD", ex);
                    return Paths.get(".");
                }
        }
    }

    /**
     * Attempt to set the JVM working directory to APP_HOME. Fails silently if
     * the property cannot be set in the running environment.
     */
    public static void setStartDir() {
        /**
         * Set the JVM working directory to the application home when possible.
         * Fail silently if the property cannot be set.
         */
        try {
            System.setProperty("user.dir", APP_HOME.toString());
        } catch (SecurityException se) {
            LOG.debug("Unable to set user.dir to APP_HOME {}", APP_HOME, se);
        }
    }

    /**
     * Ensure the working data directory exists under APP_HOME. This is
     * idempotent and safe to call on startup.
     */
    public static void workingDir() {
        /**
         * Ensure the working data directory exists under the application home.
         */
        try {
            Path studentDataDir = APP_HOME.resolve("StudentDataFiles");
            Files.createDirectories(studentDataDir);
        } catch (IOException ioe) {
            LOG.debug("Unable to create StudentDataFiles directory under {}", APP_HOME, ioe);
        }
    }

    /**
     * Create a basic folder hierarchy under DATA_ROOT for each student.
     * This will create StudentDataFiles, backups and errorLogs and a
     * per-student folder with subfolders for data sheets and materials.
     */
    public static void createFolderHierarchy() {
        /**
         * Create a basic folder hierarchy under DATA_ROOT for each student.
         * This is idempotent and will create per-student subfolders and an
         * omnibus csv file when missing.
         */
        // Create basic folders for each student in a simple roster
        List<String> students = getStudents();
        Path studentDatafilesRoot = DATA_ROOT.resolve("StudentDataFiles");
        Path studentErrorlogsRoot = DATA_ROOT.resolve("errorLogs");
        Path studentBackupsRoot = DATA_ROOT.resolve("backups");
        try {
            Files.createDirectories(studentDatafilesRoot);
            Files.createDirectories(studentErrorlogsRoot);
            Files.createDirectories(studentBackupsRoot);
        } catch (IOException ioe) {
            LOG.debug("Unable to create one or more data folders under {}", DATA_ROOT, ioe);
        }

        for (String name : students) {
            String safe = sanitize(name);
            Path studentFolder = studentDatafilesRoot.resolve(safe);
            try {
                Files.createDirectories(studentFolder.resolve("StudentDataSheets"));
                Files.createDirectories(studentFolder.resolve("StudentInstructionMaterials"));
                Files.createDirectories(studentFolder.resolve("StudentVisionAssessments"));
                Path omnibus = studentFolder.resolve("omnibusDatabase.csv");
                if (!Files.exists(omnibus)) {
                    Files.createFile(omnibus);
                }
            } catch (IOException ioe) {
                LOG.debug("Unable to create per-student folder or omnibus file for {}", name, ioe);
            }
        }
    }

    /**
     * Make a filesystem-safe folder name by stripping or replacing forbidden
     * characters.
     */
    private static String sanitize(final String s) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        // remove control characters (newline, carriage return, etc.)
        t = t.replaceAll("[\\p{Cntrl}]", "");
        // replace common filesystem-forbidden characters with underscore
        char[] forbidden = new char[]{'<','>',';',':','"','/','\\','|','?','*'};
        for (char c : forbidden) {
            t = t.replace(c, '_');
        }
        // collapse runs of whitespace into single space
        t = t.replaceAll("\\s+", " ").trim();
        // prevent names that are just dots
        if (t.matches("^[.]+$")) {
            t = "_";
        }
        return t;
    }

    /**
     * Public safe name helper for filesystem paths. Mirrors the internal
     * sanitize implementation but is callable from other packages.
     *
     * @param s input display name
     * @return sanitized filesystem-safe name (never null)
     */
    public static String safeName(final String s) {
        if (s == null) {
            return "";
        }
        return sanitize(s);
    }

    /**
     * Find the latest PNG plot file for a named student with the given prefix.
     * Returns null when no matching files exist.
    *
    * @param studentName display name of student
    * @param prefix file prefix such as "iOS" or "ScreenReader"
    * @return path to the most recently modified matching PNG, or null
     */
    public static java.nio.file.Path latestPlotPath(final String studentName, final String prefix) {
        if (studentName == null || studentName.trim().isEmpty()) {
            return null;
        }
        java.nio.file.Path dir = studentPlotsDir(studentName);
        if (!java.nio.file.Files.exists(dir)) {
            return null;
        }
        java.nio.file.Path latest = null;
        try (java.nio.file.DirectoryStream<java.nio.file.Path> ds = java.nio.file.Files.newDirectoryStream(dir, prefix + "-*.png")) {
            for (java.nio.file.Path p : ds) {
                try {
                    if (latest == null) {
                        latest = p;
                    } else {
                        java.nio.file.attribute.FileTime t1 = java.nio.file.Files.getLastModifiedTime(p);
                        java.nio.file.attribute.FileTime t2 = java.nio.file.Files.getLastModifiedTime(latest);
                        if (t1.compareTo(t2) > 0) {
                            latest = p;
                        }
                    }
                } catch (IOException ioe) {
                    LOG.debug("Error reading file metadata for {}", p, ioe);
                }
            }
        } catch (IOException ioe) {
            LOG.debug("Error listing plot directory {}", dir, ioe);
        }
        return latest;
    }

    /**
     * Return the per-student plots directory path (APP_HOME/StudentDataFiles/{safeName}/plots).
     *
     * @param studentName display name of the student
     * @return path to the student's plots directory (never null)
     */
    public static java.nio.file.Path studentPlotsDir(final String studentName) {
        return APP_HOME.resolve("StudentDataFiles").resolve(safeName(studentName)).resolve("plots");
    }

    /**
     * Return the per-student reports directory path (APP_HOME/StudentDataFiles/{safeName}/reports).
     *
     * @param studentName display name of the student
     * @return path to the student's reports directory (never null)
     */
    public static java.nio.file.Path studentReportsDir(final String studentName) {
        return APP_HOME.resolve("StudentDataFiles").resolve(safeName(studentName)).resolve("reports");
    }

    /**
     * Return the per-student collected data directory path (APP_HOME/StudentDataFiles/{safeName}/collected_data).
     *
     * @param studentName display name of the student
     * @return path to the student's collected data directory (never null)
     */
    public static java.nio.file.Path studentCollectedDataDir(final String studentName) {
        return APP_HOME.resolve("StudentDataFiles").resolve(safeName(studentName)).resolve("collected_data");
    }

    /**
     * Attempt to return a simple list of students from PROJECT_ROOT/json_Files/students.json.
     * Falls back to a single 'Test Student' entry when the file is missing or cannot be read.
     *
     * @return list of student display names (never null)
     */
    public static List<String> getStudents() {
        // Attempt to read a simple students.json in PROJECT_ROOT/json_Files/students.json
        List<String> list = new ArrayList<>();
        Path p = PROJECT_ROOT.resolve("json_Files").resolve("students.json");
        if (Files.exists(p)) {
            try {
                String text = Files.readString(p);
                // try to isolate the array portion if present
                int start = text.indexOf('[');
                int end = text.lastIndexOf(']');
                String body = (start >= 0 && end > start) ? text.substring(start, end + 1) : text;
                java.util.regex.Pattern pat = java.util.regex.Pattern.compile("\"([^\"]+)\"");
                java.util.regex.Matcher m = pat.matcher(body);
                while (m.find()) {
                    String candidate = m.group(1).trim();
                    if (!candidate.isEmpty()) {
                        list.add(candidate);
                    }
                }
            } catch (IOException ioe) {
                LOG.debug("Unable to read students.json {}", p, ioe);
            }
        }
        if (list.isEmpty()) {
            // fallback roster
            list.add("Test Student");
        }
        return list;
    }

    /**
     * Return the default student to use when none is provided by the caller.
     * This is the first entry from getStudents() or a sensible fallback when
     * the roster is empty.
     *
     * @return display name of the default student (never null)
     */
    public static String defaultStudent() {
        /**
         * Note: UI pages use this helper to provide a non-null default student
         * when constructed with a null/empty student name so that charts and
         * page logic can operate without requiring an immediate user selection.
         */
        List<String> s = getStudents();
        if (s == null || s.isEmpty()) {
            return "Demo Student";
        }
        return s.get(0);
    }
}
