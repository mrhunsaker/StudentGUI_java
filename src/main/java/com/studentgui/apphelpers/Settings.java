package com.studentgui.apphelpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Lightweight settings persistence for simple key/value preferences.
 */
public final class Settings {
    private static final Path SETTINGS_FILE = Helpers.APP_HOME.resolve("app.properties");
    private static final Properties props = new Properties();

    static {
        // load existing if present
        try (InputStream in = Files.exists(SETTINGS_FILE) ? Files.newInputStream(SETTINGS_FILE) : null) {
            if (in != null) props.load(in);
        } catch (IOException ignored) {
        }
    }

    private Settings() { throw new AssertionError(); }

    public static String get(String key, String def) {
        return props.getProperty(key, def);
    }

    public static void put(String key, String value) {
        props.setProperty(key, value == null ? "" : value);
        // persist immediately
        try (OutputStream out = Files.newOutputStream(SETTINGS_FILE)) {
            props.store(out, "application settings");
        } catch (IOException ignored) {
        }
    }
}
