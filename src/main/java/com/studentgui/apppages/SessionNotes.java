package com.studentgui.apppages;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Freeform session notes editor for general observations and reflections.
 *
 * <p>Provides a simple multi-line text area for educators to record unstructured notes
 * about a student session. This complements the structured assessment pages (Braille, Abacus, etc.)
 * by allowing qualitative observations, anecdotal records, and contextual details that don't
 * fit into numeric scoring fields.</p>
 *
 * <p><b>Typical Use Cases:</b></p>
 * <ul>
 *   <li>Recording behavioral observations (e.g., "Student showed increased frustration with Nemeth fractions today")</li>
 *   <li>Documenting environmental factors affecting performance (e.g., "Noisy classroom due to construction")</li>
 *   <li>Noting equipment issues or accommodations used (e.g., "Switched to Braille Sense due to BrailleNote malfunction")</li>
 *   <li>General reflections or instructional notes for future reference</li>
 * </ul>
 *
 * <p><b>Data Storage:</b></p>
 * <ul>
 *   <li>Notes persisted via {@link com.studentgui.apphelpers.Database#saveSessionNotes} to {@code ProgressSession.notes} column</li>
 *   <li>Associated with a SessionNotes progress type and session ID for consistent querying</li>
 *   <li>JSON export: {@code StudentDataFiles/<student>/Sessions/SessionNotes/SessionNotes-<sessionId>-<timestamp>.json}</li>
 *   <li>No plots or reports generated (text-only data)</li>
 * </ul>
 *
 * <p>The shared {@link JLineGraph} component is present for UI layout consistency but remains
 * empty (session notes are not quantitative data). This page does not implement listener interfaces
 * as it operates on static student/date parameters provided at construction time.</p>
 *
 * @see com.studentgui.apphelpers.Database#saveSessionNotes
 * @see com.studentgui.apphelpers.dto.NotesPayload
 */
public class SessionNotes extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(SessionNotes.class);
    /** Text area containing session notes entered by the user. */
    private final JTextArea notesArea;

    /** Selected student's display name used when saving session notes (may be null). */
    private final String studentNameParam;

    /** Date associated with these session notes. */
    private final LocalDate dateParam;

    /**
     * Create a SessionNotes page for the provided student and date.
     * The supplied JLineGraph is displayed below the notes editor.
     *
     * @param studentName student display name (may be null when no student selected)
     * @param date        the date this session pertains to
     * @param graph       the chart component shown beneath the notes
     */
    public SessionNotes(String studentName, LocalDate date, JLineGraph graph) {
    this.studentNameParam = (studentName == null || studentName.trim().isEmpty()) ? com.studentgui.apphelpers.Helpers.defaultStudent() : studentName;
        this.dateParam = date;
        setLayout(new BorderLayout());

    JPanel p = new JPanel(new GridBagLayout());
    JPanel view = new JPanel(new BorderLayout());
    view.add(p, BorderLayout.NORTH);
    view.setBorder(javax.swing.BorderFactory.createEmptyBorder(20,20,20,20));
    JScrollPane scroll = new JScrollPane(view);
    scroll.getAccessibleContext().setAccessibleName("Session Notes data entry scroll pane");
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets=new Insets(2,2,2,2); gbc.fill = GridBagConstraints.BOTH; gbc.anchor = GridBagConstraints.NORTHWEST;
    JLabel title = new JLabel("Session Notes", JLabel.LEFT);
    title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
    title.getAccessibleContext().setAccessibleName("Session Notes Title");
    gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=1; p.add(title, gbc);

    int globalLabel = com.studentgui.uicomp.PhaseScoreField.getGlobalLabelWidth();
    gbc.gridy=1; gbc.gridx=0; JLabel notesLabel = new JLabel("Notes:"); notesLabel.setPreferredSize(new java.awt.Dimension(globalLabel, notesLabel.getPreferredSize().height)); p.add(notesLabel, gbc);
    gbc.gridy=2; gbc.gridx=0; notesArea = new JTextArea(8,40); notesArea.setLineWrap(true); notesArea.setWrapStyleWord(true); notesArea.setToolTipText("Enter session notes for the student"); notesArea.getAccessibleContext().setAccessibleName("Session notes"); p.add(notesArea, gbc);
    notesLabel.setLabelFor(notesArea);

    gbc.gridy=3; JButton submit = new JButton("Save Session Notes");
    submit.addActionListener((ActionEvent e)-> saveNotes());
    submit.setMnemonic(KeyEvent.VK_S);
    submit.setToolTipText("Save session notes (Alt+S)");
    submit.getAccessibleContext().setAccessibleName("Save Session Notes");
    p.add(submit, gbc);

        add(scroll, BorderLayout.CENTER);

        SwingUtilities.invokeLater(()->{ p.setPreferredSize(p.getPreferredSize()); revalidate(); });

        com.studentgui.apphelpers.Helpers.createFolderHierarchy();
    }

    /**
     * Persist the contents of the session notes into the database. Ensures
     * required student and progress session records exist and writes the notes
     * to the ProgressSession.notes column.
     */
    private void saveNotes() {
        if (this.studentNameParam == null || this.studentNameParam.trim().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Please select a student before saving session notes.", "Missing student", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int studentId = com.studentgui.apphelpers.Database.getOrCreateStudent(this.studentNameParam);
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("SessionNotes");
            int sessionId = com.studentgui.apphelpers.Database.createProgressSession(studentId, ptId, this.dateParam);
            String notes = notesArea.getText();
            com.studentgui.apphelpers.Database.saveSessionNotes(sessionId, notes);
            LOG.info("Saved session notes for {}", studentNameParam);
            com.studentgui.apphelpers.UiNotifier.show("Session notes saved.");
            com.studentgui.apphelpers.dto.NotesPayload payload = new com.studentgui.apphelpers.dto.NotesPayload(sessionId, notes);
            java.nio.file.Path jsonOut = com.studentgui.apphelpers.SessionJsonWriter.writeSessionJson(this.studentNameParam, "SessionNotes", payload, sessionId);
            if (jsonOut == null) {
                LOG.warn("Unable to save SessionNotes session JSON for sessionId={}", sessionId);
            }
        } catch (SQLException ex) {
            LOG.error("Error saving session notes", ex);
            javax.swing.JOptionPane.showMessageDialog(this, "Database error saving session notes: " + ex.getMessage(), "Database error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
}
