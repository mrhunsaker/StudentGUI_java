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
 * Braille skills progression UI page.
 *
 * Displays a list of braille-related skill input fields and provides controls
 * to persist entries to the normalized database schema. The page updates a
 * shared {@link JLineGraph} to visualize recent results for the selected
 * student.
 */
public class Braille extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(Braille.class);

    /** Array of input components representing each Braille skill. */
    private final com.studentgui.uicomp.PhaseScoreField[] skillFields;
    /** Parts list for Braille (code,label) */
    private final String[][] parts;
    /** Flat list of part codes (derived from parts) */
    private final String[] partCodes;
    /** Shared graph used to plot recent results. */
    private final JLineGraph lineGraph; // Reference to the JLineGraph instance
    /** Selected student display name (may be null or placeholder). */
    private final String studentNameParam;
    /** Session date used when creating progress sessions. */
    private final LocalDate dateParam;

    /**
     * Construct the Braille skills page for a given student and date.
     *
     * @param studentName the selected student name (may be null before selection)
     * @param date the session date to use when creating a progress session
     * @param lineGraph shared graph component used to display recent results
     */
    public Braille(String studentName, LocalDate date, JLineGraph lineGraph) {
        this.lineGraph = lineGraph; // Use the passed in graph instance
        this.studentNameParam = studentName != null ? studentName : "Unknown Student";
        this.dateParam = date != null ? date : LocalDate.now();
        setLayout(new BorderLayout());

        // Detailed Braille parts (code, visible label)
        this.parts = new String[][]{
            {"P1_1","1.1. Track left to right"},{"P1_2","1.2. Track top to bottom"},{"P1_3","1.3. Discriminate shapes"},{"P1_4","1.4. Discriminate braille characters"},
            {"P2_1","2.1. Mangold Progression: G C L"},{"P2_2","2.2. Mangold Progression: D Y"},{"P2_3","2.3. Mangold Progression: A B"},{"P2_4","2.4. Mangold Progression: S"},
            {"P2_5","2.5. Mangold Progression: W"},{"P2_6","2.6. Mangold Progression: P O"},{"P2_7","2.7. Mangold Progression: K"},{"P2_8","2.8. Mangold Progression: R"},
            {"P2_9","2.9. Mangold Progression: M E"},{"P2_10","2.10. Mangold Progression: H"},{"P2_11","2.11. Mangold Progression: N X"},{"P2_12","2.12. Mangold Progression: Z F"},
            {"P2_13","2.13. Mangold Progression: U T"},{"P2_14","2.14. Mangold Progression: Q I"},{"P2_15","2.15. Mangold Progression: V J"},
            {"P3_1","3.1. Alphabetic Wordsigns"},{"P3_2","3.2. Braille Numbers"},{"P3_3","3.3. Punctuation"},{"P3_4","3.4. Strong Contractions (AND OF FOR WITH THE)"},
            {"P3_5","3.5. Strong Groupsigns (CH GH SH TH WH ED ER OU OW ST AR ING)"},{"P3_6","3.6. Strong Wordsigns (CH SH TH WH OU ST)"},{"P3_7","3.7. Lower Groupsigns (BE CON DIS)"},
            {"P3_8","3.8. Lower Groupsigns (EA BB CC FF GG)"},{"P3_9","3.9. Lower Groupsigns/Wordsigns (EN IN)"},{"P3_10","3.10. Lower Wordsigns (BE HIS WAS WERE)"},
            {"P3_11","3.11. Dot 5 Contractions"},{"P3_12","3.12. Dot 45 Contractions"},{"P3_13","3.13. Dot 456 Contractions"},{"P3_14","3.14. Final Letter Groupsigns"},
            {"P3_15","3.15. Shortform Words"},{"P4_1","4.1. Grade 1 Indicators"},{"P4_2","4.2. Capitals Indicators"},{"P4_3","4.3. Numeric Mode and Spatial math"},
            {"P4_4","4.4. Typeform Indicators (ITALIC  SCRIPT  UNDERLINE  BOLDFACE)"},{"P5_1","5.1. Page Numbering"},{"P5_2","5.2. Headings"},{"P5_3","5.3. Lists"},
            {"P5_4","5.4. Poety / Drama"},{"P6_1","6.1. Operation and Comparison Signs"},{"P6_2","6.2. Grade 1 Mode"},{"P6_3","6.3. Special Print Symbols"},
            {"P6_4","6.4. Omission Marks"},{"P6_5","6.5. Shape Indicators"},{"P6_6","6.6. Roman Numerals"},{"P6_7","6.7. Fractions"},
            {"P7_1","7.1. Grade 1 Mode and Algebra"},{"P7_2","7.2. Grade 1 Mode and Fractions"},{"P7_3","7.3. Advanced Operation and Comparison Signs"},{"P7_4","7.4. Indices"},
            {"P7_5","7.5. Roots and Radicals"},{"P7_6","7.6. Miscellaneous Shape Indicators"},{"P7_7","7.7. Functions"},{"P7_8","7.8. Greek letters"},
            {"P8_1","8.1. Functions"},{"P8_2","8.2. Modifiers  Bars  and Dots"},{"P8_3","8.3. Modifiers  Arrows  and Limits"},{"P8_4","8.4. Probability"},
            {"P8_5","8.5. Calculus: Differentiation"},{"P8_6","8.6. Calculus: Integration"},{"P8_7","8.7. Vertical Bars"}
        };
        this.partCodes = new String[this.parts.length];
        for (int i = 0; i < this.parts.length; i++) this.partCodes[i] = this.parts[i][0];

        // Panel for data entry
        JPanel dataEntryPanel = new JPanel();
        dataEntryPanel.setLayout(new GridBagLayout());
    JPanel view = new JPanel(new BorderLayout());
    view.add(dataEntryPanel, BorderLayout.NORTH);
    view.setBorder(javax.swing.BorderFactory.createEmptyBorder(20,20,20,20));
    JScrollPane dataEntryScrollPane = new JScrollPane(view);
    dataEntryScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    dataEntryScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    dataEntryScrollPane.getAccessibleContext().setAccessibleName("Braille data entry scroll pane");

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

    JLabel titleLabel = new JLabel("Braille Skills Progression");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        dataEntryPanel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.ipady = 20;
        dataEntryPanel.add(new JPanel(), gbc);

    // compute longest label width to align inputs
        String[] labels = java.util.Arrays.stream(parts).map(x->x[1]).toArray(String[]::new);
            int maxPx = com.studentgui.uicomp.PhaseScoreField.computeMaxLabelPixelWidth(titleLabel.getFont(), labels);
            com.studentgui.uicomp.PhaseScoreField.setGlobalLabelWidth(Math.min(320, Math.max(140, maxPx + 50)));
    skillFields = new com.studentgui.uicomp.PhaseScoreField[this.parts.length];
        for (int i = 0; i < this.parts.length; i++) {
            gbc.gridy = i + 2;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            com.studentgui.uicomp.PhaseScoreField skillField = new com.studentgui.uicomp.PhaseScoreField(this.parts[i][1], 0);
            skillField.setName("braille_" + this.parts[i][0]);
            skillField.getAccessibleContext().setAccessibleName(this.parts[i][1]);
            skillField.setToolTipText("Enter a numeric score for " + this.parts[i][1]);
            gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(2, 2, 2, 2);
            dataEntryPanel.add(skillField, gbc);
            skillFields[i] = skillField;
            gbc.gridx = 2; gbc.insets = new Insets(2, 0, 2, 2);
            dataEntryPanel.add(new JPanel(), gbc);
        }

    gbc.gridy = this.parts.length + 3;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weighty = 1.0;
        dataEntryPanel.add(new JPanel(), gbc);

    // Place Submit and Open Latest side-by-side (match IOS/ScreenReader style)
    gbc.gridy = this.parts.length + 4;
    gbc.weighty = 0.0;
    gbc.gridx = 0;
    gbc.gridwidth = 1;
    gbc.anchor = GridBagConstraints.WEST;
    JButton submitDataButton = new JButton("Submit Data");
    submitDataButton.setPreferredSize(new java.awt.Dimension(0, 32));
    submitDataButton.addActionListener((ActionEvent e) -> { submitData(); refreshGraph(); });
    submitDataButton.setMnemonic(KeyEvent.VK_S);
    submitDataButton.setToolTipText("Save Braille scores for the selected student (Alt+S)");
    submitDataButton.getAccessibleContext().setAccessibleName("Submit Braille Data");
    dataEntryPanel.add(submitDataButton, gbc);

    gbc.gridx = 1;
    JButton openLatestBtn = new JButton("Open Latest Plot");
    openLatestBtn.setPreferredSize(new java.awt.Dimension(0, 32));
    openLatestBtn.addActionListener((ActionEvent e) -> {
        java.nio.file.Path p = com.studentgui.apphelpers.Helpers.latestPlotPath(this.studentNameParam, "Braille");
        if (p == null) com.studentgui.apphelpers.UiNotifier.show("No Braille plot found for student");
    else { try { java.awt.Desktop.getDesktop().open(p.toFile()); } catch (java.io.IOException | UnsupportedOperationException | SecurityException ex) { com.studentgui.apphelpers.UiNotifier.show("Unable to open plot: " + p.getFileName().toString()); } }
    });
    dataEntryPanel.add(openLatestBtn, gbc);

    // consume remaining columns (if any) so layout stays compact
    gbc.gridx = 2; gbc.gridwidth = GridBagConstraints.REMAINDER; gbc.anchor = GridBagConstraints.WEST;
    dataEntryPanel.add(new JPanel(), gbc);

        add(dataEntryScrollPane, BorderLayout.CENTER);

        // Add existing graph reference
        add(lineGraph, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> {
            dataEntryPanel.setPreferredSize(dataEntryPanel.getPreferredSize());
            revalidate();
        });

        com.studentgui.apphelpers.Helpers.createFolderHierarchy();
        initDatabase();
        refreshGraph();
    }

    /**
     * Ensure the Braille progress-type and its assessment parts exist in the
     * canonical schema. Safe to call repeatedly.
     */
    private void initDatabase() {
        // Ensure normalized schema parts for Braille exist
        try {
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("Braille");
            // Use the canonical part codes defined in this.parts
            com.studentgui.apphelpers.Database.ensureAssessmentParts(ptId, this.partCodes);
        } catch (SQLException e) {
            LOG.error("Error initializing Braille parts", e);
        }
    }

    /**
     * Read entered skill values and persist them as a new progress session.
     * Performs integer validation and informs the user on invalid input.
     */
    private void submitData() {
        if (this.studentNameParam == null || this.studentNameParam.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a student before submitting Braille data.", "Missing student", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int studentId = com.studentgui.apphelpers.Database.getOrCreateStudent(this.studentNameParam);
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("Braille");
            int sessionId = com.studentgui.apphelpers.Database.createProgressSession(studentId, ptId, this.dateParam);

            String[] codes = new String[28];
            int[] scores = new int[28];
            for (int i = 0; i < 28; i++) {
                codes[i] = this.partCodes[i];
                scores[i] = skillFields[i].getValue();
            }
            com.studentgui.apphelpers.Database.insertAssessmentResults(sessionId, ptId, codes, scores);
            LOG.info("Data submitted successfully via normalized schema.");
            com.studentgui.apphelpers.UiNotifier.show("Braille data saved.");
            com.studentgui.apphelpers.dto.AssessmentPayload payload = new com.studentgui.apphelpers.dto.AssessmentPayload(sessionId, codes, scores);
            java.nio.file.Path jsonOut = com.studentgui.apphelpers.SessionJsonWriter.writeSessionJson(this.studentNameParam, "Braille", payload, sessionId);
            if (jsonOut == null) LOG.warn("Unable to save Braille session JSON for sessionId={}", sessionId);
            try {
                java.nio.file.Path out = com.studentgui.apphelpers.Helpers.APP_HOME.resolve("StudentDataFiles").resolve(com.studentgui.apphelpers.Helpers.safeName(this.studentNameParam)).resolve("plots");
                java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_DATE;
                java.nio.file.Path file = out.resolve("Braille-" + this.dateParam.format(df) + ".png");
                lineGraph.saveChart(file, 800, 400);
                LOG.info("Saved Braille plot to {}", file);
            } catch (java.io.IOException ex) {
                LOG.warn("Unable to save Braille plot image: {}", ex.toString());
            }
        } catch (SQLException e) {
            LOG.error("Unexpected error submitting braille data", e);
            JOptionPane.showMessageDialog(this, "Database error saving Braille data: " + e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * Fetch recent assessment sessions and update the shared graph view.
     */
    private void refreshGraph() {
        try {
            List<List<Integer>> allSkillValues = com.studentgui.apphelpers.Database.fetchLatestAssessmentResults(this.studentNameParam, "Braille", 5);
            // Note: pages should supply the selected student name; here the existing code used a passed-in studentName variable
            // We will try to use the first skill field's content as a student name fallback; in the UI flow this should be provided.
            // For now use a placeholder when no student is selected.
            if (allSkillValues != null && !allSkillValues.isEmpty()) {
                lineGraph.updateWithGroupedData(allSkillValues, this.partCodes);
                LOG.debug("Graph updated with data: {}", allSkillValues);
                // Save static PNG to student's plots folder and open it
                if (this.studentNameParam != null && !this.studentNameParam.trim().isEmpty()) {
                    try {
                        java.nio.file.Path out = com.studentgui.apphelpers.Helpers.APP_HOME.resolve("StudentDataFiles").resolve(com.studentgui.apphelpers.Helpers.safeName(this.studentNameParam)).resolve("plots");
                        java.nio.file.Files.createDirectories(out);
                        java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_DATE;
                        String dateStr = (this.dateParam != null ? this.dateParam.format(df) : java.time.LocalDate.now().toString());
                        java.nio.file.Path file = out.resolve("Braille-" + dateStr + ".png");
                        lineGraph.saveChart(file, 800, 400);
                        LOG.info("Saved Braille plot to {}", file);
                        try { java.awt.Desktop.getDesktop().open(file.toFile()); } catch (java.io.IOException | UnsupportedOperationException | SecurityException ex) { LOG.debug("Could not open Braille plot file: {}", ex.toString()); }
                        com.studentgui.apphelpers.UiNotifier.show("Braille plot saved to " + file.toString());
                    } catch (java.io.IOException ex) {
                        LOG.warn("Unable to save Braille plot image: {}", ex.toString());
                    }
                }
            } else {
                LOG.info("No data to plot.");
            }
        } catch (SQLException e) {
            LOG.error("SQL error refreshing braille graph", e);
        }
    }
    

}
