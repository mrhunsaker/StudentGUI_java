package com.studentgui.app;

import java.time.LocalDate;

/**
 * Simple listener interface for pages that want to be notified when the
 * application-wide selected date changes via the top-bar Apply action.
 */
public interface DateChangeListener {
    /**
     * Called when the application date has been changed by the user.
     * @param newDate the newly selected date
     */
    void dateChanged(LocalDate newDate);
}
