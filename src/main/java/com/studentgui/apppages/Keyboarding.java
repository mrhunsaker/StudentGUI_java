package com.studentgui.apppages;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keyboarding skills page. Captures program/topic/speed/accuracy results and
 * persists them to a dedicated keyboarding result table via the
 * {@code Database} helper.
 */
public class Keyboarding extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(Keyboarding.class);
    /** Text field for the program or curriculum name. */
    private final JTextField programField, topicField, speedField, accuracyField;

    /** Shared graph component (present but not used for keyboarding plotting). */
    private final JLineGraph lineGraph;

    /** Selected student's display name for saves/refreshes (may be null). */
    private final String studentNameParam;

    /** Session date associated with persisted keyboarding results. */
    private final LocalDate dateParam;

    /**
     * Construct the Keyboarding page for a specific student and session date.
     *
     * @param studentName selected student's display name (may be null)
     * @param date session date used for persisted results
     * @param lineGraph shared graph component (unused for keyboarding results)
     */
    public Keyboarding(String studentName, LocalDate date, JLineGraph lineGraph) {
        this.studentNameParam = studentName;
        this.dateParam = date;
        this.lineGraph = lineGraph;
        setLayout(new BorderLayout());

    JPanel p = new JPanel(new GridBagLayout());
    JPanel view = new JPanel(new BorderLayout());
    view.add(p, BorderLayout.NORTH);
    view.setBorder(javax.swing.BorderFactory.createEmptyBorder(20,20,20,20));
    JScrollPane scroll = new JScrollPane(view);
    scroll.getAccessibleContext().setAccessibleName("Keyboarding data entry scroll pane");
    p.setBorder(javax.swing.BorderFactory.createEmptyBorder(20,20,20,20));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets=new Insets(2,2,2,2); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.NORTHWEST;
    JLabel title = new JLabel("Keyboarding Skills", JLabel.LEFT);
    title.setFont(title.getFont().deriveFont(Font.BOLD,16));
    title.getAccessibleContext().setAccessibleName("Keyboarding Skills Title");
    gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2; p.add(title, gbc);

    gbc.gridwidth=1;
    // Normalize label width to the PhaseScoreField global width so inputs align
    int globalLabel = com.studentgui.uicomp.PhaseScoreField.getGlobalLabelWidth();
    gbc.gridy=1; gbc.gridx=0; JLabel programLabel = new JLabel("Program:"); programLabel.setPreferredSize(new Dimension(globalLabel, programLabel.getPreferredSize().height)); p.add(programLabel, gbc); gbc.gridx=1; programField = new JTextField(); programField.setPreferredSize(new Dimension(300,24)); programField.setToolTipText("Name of the program or curriculum"); programField.getAccessibleContext().setAccessibleName("Program"); p.add(programField, gbc); programLabel.setLabelFor(programField);
    gbc.gridy=2; gbc.gridx=0; JLabel topicLabel = new JLabel("Topic:"); topicLabel.setPreferredSize(new Dimension(globalLabel, topicLabel.getPreferredSize().height)); p.add(topicLabel, gbc); gbc.gridx=1; topicField = new JTextField(); topicField.setPreferredSize(new Dimension(300,24)); topicField.setToolTipText("Topic or lesson name"); topicField.getAccessibleContext().setAccessibleName("Topic"); p.add(topicField, gbc); topicLabel.setLabelFor(topicField);
    gbc.gridy=3; gbc.gridx=0; JLabel speedLabel = new JLabel("Speed (WPM):"); speedLabel.setPreferredSize(new Dimension(globalLabel, speedLabel.getPreferredSize().height)); p.add(speedLabel, gbc); gbc.gridx=1; speedField = new JTextField("0"); speedField.setPreferredSize(new Dimension(100,24)); speedField.setToolTipText("Words per minute"); speedField.getAccessibleContext().setAccessibleName("Speed (WPM)"); p.add(speedField, gbc); speedLabel.setLabelFor(speedField);
    gbc.gridy=4; gbc.gridx=0; JLabel accuracyLabel = new JLabel("Accuracy (%):"); accuracyLabel.setPreferredSize(new Dimension(globalLabel, accuracyLabel.getPreferredSize().height)); p.add(accuracyLabel, gbc); gbc.gridx=1; accuracyField = new JTextField("0"); accuracyField.setPreferredSize(new Dimension(100,24)); accuracyField.setToolTipText("Accuracy percentage"); accuracyField.getAccessibleContext().setAccessibleName("Accuracy (%)"); p.add(accuracyField, gbc); accuracyLabel.setLabelFor(accuracyField);

    gbc.gridy=5; gbc.gridx=0; gbc.gridwidth=GridBagConstraints.REMAINDER;
    JButton submit = new JButton("Submit Data");
    submit.setPreferredSize(new java.awt.Dimension(0, 32));
    submit.addActionListener((ActionEvent e)-> { submitData(); refreshGraph(); });
    submit.setToolTipText("Save keyboarding result for selected student");
    submit.setMnemonic(KeyEvent.VK_S);
    submit.getAccessibleContext().setAccessibleName("Submit Keyboarding Data");
    p.add(submit, gbc);
    gbc.gridwidth = 1;
    // Removed separate Refresh Graph button; Submit Data now triggers refreshGraph

    add(scroll, BorderLayout.CENTER);
    add(this.lineGraph, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(()->{ p.setPreferredSize(p.getPreferredSize()); revalidate(); });

        com.studentgui.apphelpers.Helpers.createFolderHierarchy();
        initDatabase();
        refreshGraph();
    }

    /**
     * Ensure the keyboarding progress type exists in the canonical schema.
     */
    private void initDatabase() {
        try {
            com.studentgui.apphelpers.Database.getOrCreateProgressType("Keyboarding");
        } catch (SQLException ex) {
            LOG.error("Error ensuring Keyboarding progress type", ex);
        }
    }

    /**
     * Validate keyboarding inputs (speed and accuracy as integers) and
     * persist a keyboarding result record for the selected student.
     */
    private void submitData() {
        if (this.studentNameParam == null || this.studentNameParam.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a student before saving keyboarding data.", "Missing student", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int studentId = com.studentgui.apphelpers.Database.getOrCreateStudent(this.studentNameParam);
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("Keyboarding");
            int sessionId = com.studentgui.apphelpers.Database.createProgressSession(studentId, ptId, this.dateParam);

            String program = programField.getText().trim();
            String topic = topicField.getText().trim();
            int speed;
            int accuracy;
            try {
                String sp = speedField.getText().trim(); speed = sp.isEmpty() ? 0 : Integer.parseInt(sp);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Please enter a whole number for Speed (WPM)", "Invalid input", JOptionPane.ERROR_MESSAGE);
                speedField.requestFocusInWindow();
                return;
            }
            try {
                String ac = accuracyField.getText().trim(); accuracy = ac.isEmpty() ? 0 : Integer.parseInt(ac);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Please enter a whole number for Accuracy (%)", "Invalid input", JOptionPane.ERROR_MESSAGE);
                accuracyField.requestFocusInWindow();
                return;
            }

            com.studentgui.apphelpers.Database.insertKeyboardingResult(sessionId, program, topic, speed, accuracy);
            LOG.info("Keyboarding data saved for {}", this.studentNameParam);
            com.studentgui.apphelpers.UiNotifier.show("Keyboarding data saved.");
            com.studentgui.apphelpers.dto.KeyboardingPayload payload = new com.studentgui.apphelpers.dto.KeyboardingPayload(sessionId, program, topic, speed, accuracy);
            java.nio.file.Path jsonOut = com.studentgui.apphelpers.SessionJsonWriter.writeSessionJson(this.studentNameParam, "Keyboarding", payload, sessionId);
            if (jsonOut == null) LOG.warn("Unable to save Keyboarding session JSON for sessionId={}", sessionId);
        } catch (SQLException ex) {
            LOG.error("DB error saving keyboarding data", ex);
            JOptionPane.showMessageDialog(this, "Database error saving keyboarding data: " + ex.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Refresh the keyboarding visualization. Currently keyboarding results are
     * stored in a separate table and this method logs the request.
     */
    private void refreshGraph() {
        LOG.info("Keyboarding refresh requested for {}", studentNameParam);
    }
}
