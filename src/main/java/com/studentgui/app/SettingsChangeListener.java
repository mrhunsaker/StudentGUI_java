package com.studentgui.app;

/**
 * Simple listener interface for application-wide settings changes.
 */
public interface SettingsChangeListener {
    /**
     * Invoked when application settings have been changed and persisted.
     * Implementations should read the desired values from the Settings
     * helper and update any runtime state accordingly.
     */
    void settingsChanged();
}
