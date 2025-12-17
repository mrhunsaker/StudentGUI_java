package com.studentgui.bootstrap;

/**
 * Lightweight bootstrapper that sets early system properties required by
 * the logging subsystem (APP_HOME and LOG_TS) before delegating to the
 * real application entry point. This ensures Logback picks up a stable
 * per-run filename for the rolling file appender.
 */
public final class Bootstrap {
    private Bootstrap() { throw new AssertionError("not instantiable"); }

    public static void main(final String[] args) {
        try {
            String appHome = com.studentgui.apphelpers.Helpers.APP_HOME.toString();
            System.setProperty("APP_HOME", appHome);
        } catch (Throwable t) {
            // Best-effort: if Helpers isn't available, fall back to a relative path
            System.setProperty("APP_HOME", "app_home");
        }
        // Ensure a stable per-run timestamp for Logback file naming. Use
        // the same yyyyMMddHHmmss pattern that logback's <timestamp>
        // element uses so filenames match when possible.
        try {
            java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(java.time.ZoneOffset.UTC);
            String ts = df.format(java.time.Instant.now());
            System.setProperty("LOG_TS", ts);
        } catch (Exception ex) {
            System.setProperty("LOG_TS", String.valueOf(java.time.Instant.now().getEpochSecond()));
        }

        // Create logs directory early to avoid races when Logback opens the file
        try {
            java.nio.file.Path logs = java.nio.file.Paths.get(System.getProperty("APP_HOME")).resolve("logs");
            java.nio.file.Files.createDirectories(logs);
        } catch (Exception ex) {
            // ignore - best effort
        }

        // Delegate to the main application
        com.studentgui.app.Main.main(args);
    }
}
