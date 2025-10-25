package com.studentgui.app;

/**
 * Listener for application-wide student selection changes.
 */
public interface StudentChangeListener {
    /**
     * Called when the application selected student has changed.
     * @param newStudent the newly selected student's display name (may be null)
     */
    void studentChanged(String newStudent);
}
