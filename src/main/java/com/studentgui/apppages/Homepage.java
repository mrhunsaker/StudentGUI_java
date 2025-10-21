package com.studentgui.apppages;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * Simple homepage panel with application overview/help text.
 *
 * <p>Provides a small, static help and overview text area that can be
 * embedded into the main application frame.</p>
 */
public class Homepage {
    /**
     * Create the homepage panel which contains a title and an overview/help
     * text area.
     *
     * @return a ready-to-add {@link JPanel} containing the application overview
     */
    public static JPanel create() {
        JPanel p = new JPanel(new BorderLayout());
    JLabel title = new JLabel("Student Skills Progressions", SwingConstants.LEFT);
        title.setFont(title.getFont().deriveFont(24f));
        title.getAccessibleContext().setAccessibleName("Student Skills Progressions title");
        title.setName("homepage_title");
        p.add(title, BorderLayout.NORTH);

    JTextArea body = new JTextArea();
                body.setLineWrap(true);
                body.setWrapStyleWord(true);
                String text = """
                                Welcome to the Student Skills Progressions application.

                                This tool helps educators track and record student progress across a set of vision and access skill areas (Braille, Abacus, Digital Literacy, iOS access, Screen Reader, CVI, Keyboarding, and more).

                                How to use:
                                    1. Select a student from the Student dropdown at the top-left.
                                    2. Use the Date field to set the session date and click Apply to recreate pages for that date.
                                    3. Navigate to a skill page using the Navigate menu (or the top control bar). Each skill page contains standardized rows for entering phase/score values.
                                    4. Enter assessment data and notes on each page. Use the Save / Submit buttons on pages where available to persist data to the local SQLite database.
                                    5. The shared graph shows progress trends for the selected student. Session notes and contact logs provide a place for free-form observations and structured contact records.

                                Data storage and export:
                                    • All data is stored locally in a SQLite database under the application data folder.
                                    • Use the Instructional Materials page to open and manage student-facing materials and reports.

                                Support and workflow tips:
                                    • Start each session by verifying the student and date, then move through skill pages, entering scores and notes.
                                    • Use Contact Log to record family/guardian contact; structured fields make later reporting easier.
                                    • If you need to reset or recreate pages for a student/date, use the Apply button after changing the date.

                                Thanks for using the Student Skills Progressions application.
                                """;
                body.setText(text);
        body.setEditable(false);
        body.setToolTipText("Overview and quick help about the application");
        body.getAccessibleContext().setAccessibleName("Homepage overview");
        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.getAccessibleContext().setAccessibleName("Homepage overview scroll pane");
        body.setName("homepage_body");
        p.add(bodyScroll, BorderLayout.CENTER);
        return p;
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Homepage() {
        throw new AssertionError("Not instantiable");
    }
}
