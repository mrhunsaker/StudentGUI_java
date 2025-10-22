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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abacus skills progression UI page.
 * <p>
 * Presents a scrollable list of abacus-related skill input fields for a
 * particular student and date. Values entered here are persisted via the
 * centralized database helper into the normalized schema and can be plotted
 * using the shared {@link JLineGraph} component.
 * </p>
 */
public class Abacus extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(Abacus.class);

    /** Array of input components for each skill. */
    private final com.studentgui.uicomp.PhaseScoreField[] skillFields;
    /** Canonical list of abacus assessment parts: code and display label. */
    private final String[][] parts;
    /** Shared graph component used to visualize recent results. */
    private final JLineGraph lineGraph; // Reference to the JLineGraph instance
    /** Selected student display name (may be null). */
    private final String studentNameParam;
    /** Session date associated with persisted progress. */
    private final LocalDate dateParam;

    /**
     * Construct the Abacus page for the given student and session date.
     *
     * @param studentName the selected student's display name (may be null before selection)
     * @param date the date to associate with created progress sessions
     * @param lineGraph the shared graph component used to visualize results
     */
    public Abacus(String studentName, LocalDate date, JLineGraph lineGraph) {
        this.studentNameParam = studentName;
        this.dateParam = date;
        this.lineGraph = lineGraph; // Use the passed in graph instance
        setLayout(new BorderLayout());

        // Initialize skills array and layout using canonical abacus parts
        this.parts = new String[][]{
            {"P1_1","1.1 Setting Numbers"},{"P1_2","1.2 Clearing Beads"},{"P1_3","1.3 Place Value"},{"P1_4","1.4 Vocabulary"},
            {"P2_1","2.1 Addition of Single Digit Numbers"},{"P2_2","2.2 Direct Addition"},{"P2_3","2.3 Indirect Addition"},
            {"P3_1","3.1 Subtraction of Single Digit Numbers"},{"P3_2","3.2 Direct Subtraction"},{"P3_3","3.3 Indirect Subtraction"},
            {"P4_1","4.1 Multiplication – 2+ Digit Multiplicand 1-Digit Multiplier"},{"P4_2","4.2 Multiplication – 2+ Digit Multiplicand AND Multiplier"},
            {"P5_1","5.1 Division – 2+ Digit Dividend 1-Digit Divisor"},{"P5_2","5.2 Division – 2+ Digit Dividend AND 1 Digit Divisor"},
            {"P6_1","6.1 Addition of Decimals"},{"P6_2","6.2 Subtraction of Decimals"},{"P6_3","6.3 Multiplication of Decimals"},{"P6_4","6.4 Division of Decimals"},
            {"P7_1","7.1 Addition of Fractions"},{"P7_2","7.2 Subtraction of Fractions"},{"P7_3","7.3 Multiplication of Fractions"},{"P7_4","7.4 Division of Fractions"},
            {"P8_1","8.1 Percent"},{"P8_2","8.2 Square Root"}
        };

        // Panel for data entry
        JPanel dataEntryPanel = new JPanel();
        dataEntryPanel.setLayout(new GridBagLayout());
    JPanel view = new JPanel(new BorderLayout());
    view.add(dataEntryPanel, BorderLayout.NORTH);
    view.setBorder(javax.swing.BorderFactory.createEmptyBorder(20,20,20,20));
    JScrollPane dataEntryScrollPane = new JScrollPane(view);
    dataEntryScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    dataEntryScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    dataEntryScrollPane.getAccessibleContext().setAccessibleName("Abacus data entry scroll pane");

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

    JLabel titleLabel = new JLabel("Abacus Skills Progression");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        dataEntryPanel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.ipady = 20;
        dataEntryPanel.add(new JPanel(), gbc);

    // visual spacing controlled by PhaseScoreField and layout

    String[] labels = java.util.Arrays.stream(this.parts).map(x->x[1]).toArray(String[]::new);
        int maxPx = com.studentgui.uicomp.PhaseScoreField.computeMaxLabelPixelWidth(titleLabel.getFont(), labels);
        com.studentgui.uicomp.PhaseScoreField.setGlobalLabelWidth(Math.min(320, Math.max(140, maxPx + 50)));
    skillFields = new com.studentgui.uicomp.PhaseScoreField[this.parts.length];
    for (int i = 0; i < this.parts.length; i++) {
            gbc.gridy = i + 2;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            com.studentgui.uicomp.PhaseScoreField field = new com.studentgui.uicomp.PhaseScoreField(this.parts[i][1], 0);
            field.setName("abacus_" + this.parts[i][0]);
            field.getAccessibleContext().setAccessibleName(this.parts[i][1]);
            field.setToolTipText("Enter a numeric score for " + this.parts[i][1]);
            gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(2, 2, 2, 2);
            dataEntryPanel.add(field, gbc);
            skillFields[i] = field;
            gbc.gridx = 2; gbc.gridwidth = 1; gbc.insets = new Insets(2, 0, 2, 2);
            dataEntryPanel.add(new JPanel(), gbc);
        }

    gbc.gridy = this.parts.length + 3;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weighty = 1.0;
        dataEntryPanel.add(new JPanel(), gbc);

    // Place Submit and Open Latest side-by-side with IOS-like height
    gbc.gridy = this.parts.length + 4;
    gbc.weighty = 0.0;
    gbc.gridx = 0;
    gbc.gridwidth = 1;
    JButton submitDataButton = new JButton("Submit Data");
    submitDataButton.setPreferredSize(new java.awt.Dimension(0, 32));
    submitDataButton.addActionListener((ActionEvent e) -> { submitData(); refreshGraph(); });
    submitDataButton.setMnemonic(KeyEvent.VK_S);
    submitDataButton.setToolTipText("Save Abacus scores for the selected student (Alt+S)");
    submitDataButton.getAccessibleContext().setAccessibleName("Submit Abacus Data");
    dataEntryPanel.add(submitDataButton, gbc);

    gbc.gridx = 1;
    JButton openLatestBtn = new JButton("Open Latest Plot");
    openLatestBtn.setPreferredSize(new java.awt.Dimension(0, 32));
    openLatestBtn.addActionListener((ActionEvent e) -> {
        java.nio.file.Path p = com.studentgui.apphelpers.Helpers.latestPlotPath(this.studentNameParam, "Abacus");
        if (p == null) com.studentgui.apphelpers.UiNotifier.show("No Abacus plot found for student");
    else { try { java.awt.Desktop.getDesktop().open(p.toFile()); } catch (java.io.IOException | UnsupportedOperationException | SecurityException ex) { com.studentgui.apphelpers.UiNotifier.show("Unable to open plot: " + p.getFileName().toString()); } }
    });
    dataEntryPanel.add(openLatestBtn, gbc);

    gbc.gridx = 2; gbc.gridwidth = GridBagConstraints.REMAINDER;
    dataEntryPanel.add(new JPanel(), gbc);

        add(dataEntryScrollPane, BorderLayout.CENTER);

        // Add existing graph reference
        add(lineGraph, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> {
            dataEntryPanel.setPreferredSize(dataEntryPanel.getPreferredSize());
            revalidate();
        });

        // Ensure application folders and DB schema exist before DB operations
        com.studentgui.apphelpers.Helpers.createFolderHierarchy();
        initDatabase();
        refreshGraph();
    }

    /**
     * Ensure the canonical progress-type and assessment parts for Abacus exist
     * in the normalized database schema. Safe to call multiple times.
     */
    private void initDatabase() {
        try {
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("Abacus");
            String[] codes = new String[28];
            for (int i = 0; i < 28; i++) codes[i] = "P" + (i+1);
            com.studentgui.apphelpers.Database.ensureAssessmentParts(ptId, codes);
        } catch (SQLException e) {
            LOG.error("SQL error initializing Abacus parts", e);
        }
    }

    /**
     * Read input fields, validate numeric input, and persist the values as a
     * new progress session for the selected student.
     */
    private void submitData() {
        if (studentNameParam == null || studentNameParam.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a student before submitting Abacus data.", "Missing student", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int studentId = com.studentgui.apphelpers.Database.getOrCreateStudent(studentNameParam);
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("Abacus");
            int sessionId = com.studentgui.apphelpers.Database.createProgressSession(studentId, ptId, dateParam);

            String[] codes = new String[this.parts.length];
            int[] scores = new int[this.parts.length];
            for (int i = 0; i < this.parts.length; i++) {
                codes[i] = this.parts[i][0];
                scores[i] = skillFields[i].getValue();
            }
            com.studentgui.apphelpers.Database.insertAssessmentResults(sessionId, ptId, codes, scores);
            LOG.info("Data submitted successfully via normalized schema.");
            com.studentgui.apphelpers.UiNotifier.show("Abacus data saved.");
            // Also persist this session as a JSON file in the student's folder (timestamped per-session)
            com.studentgui.apphelpers.dto.AssessmentPayload payload = new com.studentgui.apphelpers.dto.AssessmentPayload(sessionId, codes, scores);
            java.nio.file.Path jsonOut = com.studentgui.apphelpers.SessionJsonWriter.writeSessionJson(this.studentNameParam, "Abacus", payload, sessionId);
            if (jsonOut == null) LOG.warn("Unable to save Abacus session JSON for sessionId={}", sessionId);
        } catch (SQLException e) {
            LOG.error("SQL error in submitData", e);
            JOptionPane.showMessageDialog(this, "Database error saving Abacus data: " + e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Load recent assessment sessions for the selected student and update the
     * shared {@link JLineGraph} with the returned metric series.
     */
    private void refreshGraph() {
        try {
            List<List<Integer>> allSkillValues = com.studentgui.apphelpers.Database.fetchLatestAssessmentResults(studentNameParam, "Abacus", 5);
            if (allSkillValues != null && !allSkillValues.isEmpty()) {
                lineGraph.updateWithData(allSkillValues);
                LOG.debug("Graph updated with data: {}", allSkillValues);
                if (this.studentNameParam != null && !this.studentNameParam.trim().isEmpty()) {
                    try {
                        java.nio.file.Path out = com.studentgui.apphelpers.Helpers.APP_HOME.resolve("StudentDataFiles").resolve(com.studentgui.apphelpers.Helpers.safeName(this.studentNameParam)).resolve("plots");
                        java.nio.file.Files.createDirectories(out);
                        java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_DATE;
                        String dateStr = (this.dateParam != null ? this.dateParam.format(df) : java.time.LocalDate.now().toString());
                        java.nio.file.Path file = out.resolve("Abacus-" + dateStr + ".png");
                        lineGraph.saveChart(file, 800, 400);
                        LOG.info("Saved Abacus plot to {}", file);
                        try { java.awt.Desktop.getDesktop().open(file.toFile()); } catch (java.io.IOException | UnsupportedOperationException | SecurityException ex) { LOG.debug("Could not open Abacus plot file: {}", ex.toString()); }
                        com.studentgui.apphelpers.UiNotifier.show("Abacus plot saved to " + file.toString());
                    } catch (java.io.IOException ex) {
                        LOG.warn("Unable to save Abacus plot image: {}", ex.toString());
                    }
                }
            } else {
                LOG.info("No data to plot.");
            }
        } catch (SQLException e) {
            LOG.error("SQL error refreshing graph", e);
        }
    }
    

}
