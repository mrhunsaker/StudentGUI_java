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
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Settings.class);

    static {
        // load existing if present
        try (InputStream in = Files.exists(SETTINGS_FILE) ? Files.newInputStream(SETTINGS_FILE) : null) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ioe) {
            LOG.debug("Could not load settings from {}", SETTINGS_FILE, ioe);
        }
    }

    private Settings() { throw new AssertionError(); }

    /**
     * Get a persisted setting value or return a default when missing.
     *
     * @param key setting key
     * @param def default value when key is absent
     * @return stored value or default
     */
    public static String get(final String key, final String def) {
        return props.getProperty(key, def);
    }

    /**
     * Store a setting value and persist to disk immediately.
     *
     * @param key setting key
     * @param value setting value (null treated as empty string)
     */
    public static void put(final String key, final String value) {
        props.setProperty(key, value == null ? "" : value);
        // persist immediately
        try (OutputStream out = Files.newOutputStream(SETTINGS_FILE)) {
            props.store(out, "application settings");
        } catch (IOException ioe) {
            LOG.debug("Could not persist settings to {}", SETTINGS_FILE, ioe);
        }
    }
}
