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
 * Braille note-taking skills progression page.
 * <p>
 * Presents a scrollable list of skill fields for a student and allows
 * submission of scores into the canonical (normalized) SQLite schema.
 * The page also displays a shared {@link JLineGraph} instance to visualize
 * recent results.
 * </p>
 */
public class BrailleNote extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(BrailleNote.class);

    /** Inputs for each BrailleNote skill. */
    private final com.studentgui.uicomp.PhaseScoreField[] skillFields;
    /** Canonical assessment part codes and labels for BrailleNote. */
    private final String[][] parts;
    /** Shared graph component for plotting results. */
    private final JLineGraph lineGraph; // Reference to the JLineGraph instance
    /** Display name of the selected student (may be null). */
    private final String studentNameParam;
    /** Session date associated with persisted progress. */
    private final LocalDate dateParam;

    /**
     * Create the BrailleNote page for a specific student and date.
     *
     * @param studentName the selected student name (may be null until a student is chosen)
     * @param date the date for the session (used when creating a progress session)
     * @param lineGraph shared graph component used to display recent results
     */
    public BrailleNote(String studentName, LocalDate date, JLineGraph lineGraph) {
        this.studentNameParam = studentName;
        this.dateParam = date;
        this.lineGraph = lineGraph; // Use the passed in graph instance
        setLayout(new BorderLayout());

    this.parts = new String[][]{
            {"P1_1","1.1 Physical Layout"},{"P1_2","1.2 Setup/Universal Commands"},{"P1_3","1.3 BNT+ Navigation"},{"P1_4","1.4 File Management"},{"P1_5","1.5 Word Processor"},{"P1_6","1.6 Email"},{"P1_7","1.7 Internet"},{"P1_8","1.8 Calculator"},{"P1_9","1.9 KeyMath"},
            {"P2_1","2.1 Calendar"},{"P2_2","2.2 KeyBRF"},{"P2_3","2.3 KeyFiles"},{"P2_4","2.4 KeyMail"},{"P2_5","2.5 KeyWeb"},{"P2_6","2.6 KeyCalc"},{"P2_7","2.7 KeyWord"},
            {"P3_1","3.1 KeySlides"},{"P3_2","3.2 KeyCode"},{"P3_3","3.3 Third Party Apps"},{"P3_4","3.4 Braille Input"},{"P3_5","3.5 Braille Output"},{"P3_6","3.6 Settings"},{"P3_7","3.7 Accessibility"},
            {"P4_1","4.1 Advanced File Management"},{"P4_2","4.2 Cloud Integration"},{"P4_3","4.3 Device Maintenance"},
            {"P5_1","5.1 Collaboration"},{"P5_2","5.2 Export/Import"},{"P5_3","5.3 Printing"},{"P5_4","5.4 Backup"},
            {"P6_1","6.1 App Installation"},{"P6_2","6.2 App Updates"},{"P6_3","6.3 Troubleshooting"},
            {"P7_1","7.1 Custom Shortcuts"},{"P7_2","7.2 Macros"},{"P7_3","7.3 Scripting"},{"P7_4","7.4 Automation"},
            {"P8_1","8.1 Bluetooth Devices"},{"P8_2","8.2 USB Devices"},{"P8_3","8.3 External Displays"},{"P8_4","8.4 Audio Output"},{"P8_5","8.5 Video Output"},
            {"P9_1","9.1 Security"},{"P9_2","9.2 User Accounts"},{"P9_3","9.3 Parental Controls"},{"P9_4","9.4 Network Settings"},
            {"P10_1","10.1 Speech Settings"},{"P10_2","10.2 Voice Profiles"},{"P10_3","10.3 Language Support"},
            {"P11_1","11.1 Firmware Updates"},{"P11_2","11.2 Diagnostics"},{"P11_3","11.3 Logs"},{"P11_4","11.4 Support"},{"P11_5","11.5 Warranty"},
            {"P12_1","12.1 Community Resources"},{"P12_2","12.2 Online Help"},{"P12_3","12.3 User Forums"},{"P12_4","12.4 Feedback"}
        };

        // Panel for data entry
        JPanel dataEntryPanel = new JPanel();
        dataEntryPanel.setLayout(new GridBagLayout());
    JScrollPane dataEntryScrollPane = new JScrollPane(dataEntryPanel);
    dataEntryScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    dataEntryScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    dataEntryScrollPane.getAccessibleContext().setAccessibleName("BrailleNote data entry scroll pane");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

    JLabel titleLabel = new JLabel("BrailleNote Skills Progression");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        dataEntryPanel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.ipady = 20;
        dataEntryPanel.add(new JPanel(), gbc);

    // layout spacing handled by PhaseScoreField

    // compute pixel width using font metrics so labels align precisely
    String[] labelsArr = java.util.Arrays.stream(parts).map(x->x[1]).toArray(String[]::new);
    int maxPx = com.studentgui.uicomp.PhaseScoreField.computeMaxLabelPixelWidth(titleLabel.getFont(), labelsArr);
    com.studentgui.uicomp.PhaseScoreField.setGlobalLabelWidth(Math.min(320, Math.max(140, maxPx + 50)));
    skillFields = new com.studentgui.uicomp.PhaseScoreField[parts.length];
        for (int i = 0; i < parts.length; i++) {
            gbc.gridy = i + 2;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            com.studentgui.uicomp.PhaseScoreField field = new com.studentgui.uicomp.PhaseScoreField(parts[i][1], 0);
            field.setName("braillenote_" + parts[i][0]);
            field.getAccessibleContext().setAccessibleName(parts[i][1]);
            field.setToolTipText("Enter a numeric score for " + parts[i][1]);
            gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(5, 5, 5, 5);
            dataEntryPanel.add(field, gbc);
            skillFields[i] = field;
            gbc.gridx = 2; gbc.gridwidth = 1; gbc.insets = new Insets(5, 0, 5, 5);
            dataEntryPanel.add(new JPanel(), gbc);
        }

    gbc.gridy = parts.length + 3;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weighty = 1.0;
        dataEntryPanel.add(new JPanel(), gbc);

    gbc.gridy = parts.length + 4;
        gbc.weighty = 0.0;
        // layout spacing handled by PhaseScoreField
    // Place Submit and Open Latest side-by-side like IOS/ScreenReader
    gbc.gridy = parts.length + 4; gbc.gridx = 0; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
    JButton submitDataButton = new JButton("Submit Data");
    submitDataButton.setPreferredSize(new java.awt.Dimension(0, 32));
    submitDataButton.addActionListener((ActionEvent e) -> { submitData(); refreshGraph(); });
    submitDataButton.setMnemonic(KeyEvent.VK_S);
    submitDataButton.setToolTipText("Save BrailleNote scores for the selected student (Alt+S)");
    submitDataButton.getAccessibleContext().setAccessibleName("Submit BrailleNote Data");
    dataEntryPanel.add(submitDataButton, gbc);

    gbc.gridx = 1; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
    JButton openLatestBtn = new JButton("Open Latest Plot");
    openLatestBtn.setPreferredSize(new java.awt.Dimension(0, 32));
    openLatestBtn.addActionListener((ActionEvent e) -> {
        java.nio.file.Path p = com.studentgui.apphelpers.Helpers.latestPlotPath(this.studentNameParam, "BrailleNote");
        if (p == null) com.studentgui.apphelpers.UiNotifier.show("No BrailleNote plot found for student");
    else { try { java.awt.Desktop.getDesktop().open(p.toFile()); } catch (java.io.IOException | UnsupportedOperationException | SecurityException ex) { com.studentgui.apphelpers.UiNotifier.show("Unable to open plot: " + p.getFileName().toString()); } }
    });
    dataEntryPanel.add(openLatestBtn, gbc);

        add(dataEntryScrollPane, BorderLayout.CENTER);

        // Add existing graph reference
        add(lineGraph, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> {
            dataEntryPanel.setPreferredSize(dataEntryPanel.getPreferredSize());
            revalidate();
        });

        // Ensure application folders and DB schema exist
        com.studentgui.apphelpers.Helpers.createFolderHierarchy();
        initDatabase();
        refreshGraph();
    }

    /**
     * Ensure the progress-type and assessment part rows for BrailleNote exist
     * in the normalized schema. This is safe to call repeatedly.
     */
    private void initDatabase() {
        try {
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("BrailleNote");
            String[] codes = new String[28];
            for (int i = 0; i < 28; i++) codes[i] = "P" + (i+1);
            com.studentgui.apphelpers.Database.ensureAssessmentParts(ptId, codes);
        } catch (SQLException e) {
            LOG.error("SQL error initializing braille note parts", e);
        }
    }

    /**
     * Read the values entered into the skill fields and persist them to the
     * database as a new progress session. Validation is performed to ensure
     * numeric integer input; users are prompted on invalid values.
     */
    private void submitData() {
        if (this.studentNameParam == null || this.studentNameParam.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a student before submitting BrailleNote data.", "Missing student", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int studentId = com.studentgui.apphelpers.Database.getOrCreateStudent(this.studentNameParam);
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("BrailleNote");
            int sessionId = com.studentgui.apphelpers.Database.createProgressSession(studentId, ptId, this.dateParam);
            String[] codes = new String[parts.length];
            int[] scores = new int[parts.length];
            for (int i = 0; i < parts.length && i < skillFields.length; i++) {
                codes[i] = parts[i][0];
                scores[i] = skillFields[i].getValue();
            }
            com.studentgui.apphelpers.Database.insertAssessmentResults(sessionId, ptId, codes, scores);
            LOG.info("Data submitted successfully via normalized schema.");
            com.studentgui.apphelpers.UiNotifier.show("BrailleNote data saved.");
            com.studentgui.apphelpers.dto.AssessmentPayload payload = new com.studentgui.apphelpers.dto.AssessmentPayload(sessionId, codes, scores);
            java.nio.file.Path jsonOut = com.studentgui.apphelpers.SessionJsonWriter.writeSessionJson(this.studentNameParam, "BrailleNote", payload, sessionId);
            if (jsonOut == null) LOG.warn("Unable to save BrailleNote session JSON for sessionId={}", sessionId);
        } catch (SQLException e) {
            LOG.error("SQL error saving braille note data", e);
            JOptionPane.showMessageDialog(this, "Database error saving BrailleNote data: " + e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Query the most recent assessment sessions for this student and update
     * the shared {@link JLineGraph} with the returned values.
     */
    private void refreshGraph() {
        try {
            List<List<Integer>> allSkillValues = com.studentgui.apphelpers.Database.fetchLatestAssessmentResults(studentNameParam, "BrailleNote", 5);
            if (allSkillValues != null && !allSkillValues.isEmpty()) {
                lineGraph.updateWithData(allSkillValues);
                LOG.debug("Graph updated with data: {}", allSkillValues);
                if (this.studentNameParam != null && !this.studentNameParam.trim().isEmpty()) {
                    try {
                        java.nio.file.Path out = com.studentgui.apphelpers.Helpers.APP_HOME.resolve("StudentDataFiles").resolve(com.studentgui.apphelpers.Helpers.safeName(this.studentNameParam)).resolve("plots");
                        java.nio.file.Files.createDirectories(out);
                        java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_DATE;
                        String dateStr = (this.dateParam != null ? this.dateParam.format(df) : java.time.LocalDate.now().toString());
                        java.nio.file.Path file = out.resolve("BrailleNote-" + dateStr + ".png");
                        lineGraph.saveChart(file, 800, 400);
                        LOG.info("Saved BrailleNote plot to {}", file);
                        try { java.awt.Desktop.getDesktop().open(file.toFile()); } catch (java.io.IOException | UnsupportedOperationException | SecurityException ex) { LOG.debug("Could not open BrailleNote plot file: {}", ex.toString()); }
                        com.studentgui.apphelpers.UiNotifier.show("BrailleNote plot saved to " + file.toString());
                    } catch (java.io.IOException ex) {
                        LOG.warn("Unable to save BrailleNote plot image: {}", ex.toString());
                    }
                }
            } else {
                LOG.info("No data to plot.");
            }
        } catch (SQLException e) {
            LOG.error("SQL error refreshing braille note graph", e);
        }
    }
    

}
