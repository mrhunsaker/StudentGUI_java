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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observations page for recording freeform observational notes.
 */
public class Observations extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(Observations.class);
    /** Multi-line text area for entering observational notes. */
    private final JTextArea notesArea;

    /** Selected student's display name (may be null) for this observation session. */
    private final String studentNameParam;

    /** Date associated with the recorded observations. */
    private final LocalDate dateParam;

    /**
     * Create an Observations page for the given student and date.
     *
     * @param studentName student display name (may be null when no student selected)
     * @param date        the date this observation applies to
     */
    public Observations(String studentName, LocalDate date) {
        this.studentNameParam = studentName;
        this.dateParam = date;
        setLayout(new BorderLayout());

    JPanel p = new JPanel(new GridBagLayout());
    JPanel view = new JPanel(new BorderLayout());
    view.add(p, BorderLayout.NORTH);
    view.setBorder(javax.swing.BorderFactory.createEmptyBorder(20,20,20,20));
    JScrollPane scroll = new JScrollPane(view);
    scroll.getAccessibleContext().setAccessibleName("Observations data entry scroll pane");
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets=new Insets(2,2,2,2); gbc.fill = GridBagConstraints.BOTH; gbc.anchor = GridBagConstraints.NORTHWEST;
    JLabel title = new JLabel("Observations", JLabel.LEFT);
    title.setFont(title.getFont().deriveFont(Font.BOLD,16));
    title.getAccessibleContext().setAccessibleName("Observations Title");
    gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=1; p.add(title, gbc);

    gbc.gridy=1; gbc.gridx=0; JLabel notesLabel = new JLabel("Notes:"); p.add(notesLabel, gbc);
    gbc.gridy=2; gbc.gridx=0; notesArea = new JTextArea(8,40); notesArea.setLineWrap(true); notesArea.setWrapStyleWord(true); notesArea.setToolTipText("Enter observational notes for the student"); notesArea.getAccessibleContext().setAccessibleName("Observations notes"); p.add(notesArea, gbc);
    notesLabel.setLabelFor(notesArea);

    // Filler so the scroll content has room and the form is visible (prevents
    // the shared graph in SOUTH from visually dominating the view)
    gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = GridBagConstraints.REMAINDER; gbc.weighty = 1.0;
    p.add(new JPanel(), gbc);
    gbc.weighty = 0.0; gbc.gridwidth = 1;

    gbc.gridy = 4; JButton submit = new JButton("Save Notes");
    submit.addActionListener((ActionEvent e)-> saveNotes());
    submit.setMnemonic(KeyEvent.VK_S);
    submit.setToolTipText("Save observational notes (Alt+S)");
    submit.getAccessibleContext().setAccessibleName("Save Observations Notes");
    gbc.gridx = 0; gbc.anchor = GridBagConstraints.WEST;
    p.add(submit, gbc);
    // consume remaining columns so layout stays consistent
    gbc.gridx = 1; gbc.gridwidth = GridBagConstraints.REMAINDER; p.add(new JPanel(), gbc);

    add(scroll, BorderLayout.CENTER);

        SwingUtilities.invokeLater(()->{ p.setPreferredSize(p.getPreferredSize()); revalidate(); });

        com.studentgui.apphelpers.Helpers.createFolderHierarchy();
    }

    /**
     * Persist the contents of the notes area into the canonical database.
     * Creates or re-uses the student, progress type and session records as needed.
     */
    private void saveNotes() {
        if (this.studentNameParam == null || this.studentNameParam.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a student before saving observations.", "Missing student", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int studentId = com.studentgui.apphelpers.Database.getOrCreateStudent(this.studentNameParam);
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("Observations");
            int sessionId = com.studentgui.apphelpers.Database.createProgressSession(studentId, ptId, this.dateParam);
            String notes = notesArea.getText();
            com.studentgui.apphelpers.Database.insertAssessmentResults(sessionId, ptId, new String[]{"OBS_NOTE"}, new int[]{0});
            // store the notes in the ProgressSession.notes column via helper
            com.studentgui.apphelpers.Database.saveSessionNotes(sessionId, notes);
            LOG.info("Saved observations for {}", studentNameParam);
            com.studentgui.apphelpers.UiNotifier.show("Observations saved.");
            com.studentgui.apphelpers.dto.NotesPayload payload = new com.studentgui.apphelpers.dto.NotesPayload(sessionId, notes);
            java.nio.file.Path jsonOut = com.studentgui.apphelpers.SessionJsonWriter.writeSessionJson(this.studentNameParam, "Observations", payload, sessionId);
            if (jsonOut == null) LOG.warn("Unable to save Observations session JSON for sessionId={}", sessionId);
        } catch (SQLException ex) {
            LOG.error("Error saving observations", ex);
            JOptionPane.showMessageDialog(this, "Database error saving observations: " + ex.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
