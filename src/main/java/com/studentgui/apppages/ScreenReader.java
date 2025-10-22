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
 * ScreenReader skills progression page.
 *
 * Displays a form of numeric fields representing screen reader skill codes
 * and provides persistence of those values to the canonical database. A
 * supplied {@link com.studentgui.apppages.JLineGraph} is used to render
 * recent results below the form.
 */
public class ScreenReader extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(ScreenReader.class);
    /** Array of input fields corresponding to ScreenReader assessment parts. */
    private final com.studentgui.uicomp.PhaseScoreField[] skillFields;
    /** Canonical parts (code + label) for ScreenReader. */
    private final String[][] parts;

    /** Shared graph component used to visualize recent ScreenReader sessions. */
    private final JLineGraph lineGraph;

    /** Selected student's display name used for saves and plots (may be null). */
    private final String studentNameParam;

    /** Session date associated with entries made on this page. */
    private final LocalDate dateParam;

    /**
     * Construct a ScreenReader page bound to a student and date.
     * The provided JLineGraph is used to render recent assessment results.
     *
     * @param studentName the student display name (may be null to indicate no selection)
     * @param date        the date associated with the session
     * @param lineGraph   chart component used to display recent results
     */
    public ScreenReader(String studentName, LocalDate date, JLineGraph lineGraph) {
        this.studentNameParam = studentName;
        this.dateParam = date;
        this.lineGraph = lineGraph;
        setLayout(new BorderLayout());

        this.parts = new String[][]{
            {"P1_1","1.1 Basic Navigation"},{"P1_2","1.2 Read Labels"},{"P1_3","1.3 Interact Controls"},{"P1_4","1.4 Form Entry"},{"P1_5","1.5 Table Navigation"},{"P1_6","1.6 Headings"},
            {"P2_1","2.1 Links"},{"P2_2","2.2 Lists"},{"P2_3","2.3 Images"},{"P2_4","2.4 Annotations"},
            {"P3_1","3.1 Document Structure"},{"P3_2","3.2 Styles"},{"P3_3","3.3 Tables"},{"P3_4","3.4 Charts"},{"P3_5","3.5 Advanced Shortcuts"},{"P3_6","3.6 Scripting"},{"P3_7","3.7 Third Party Apps"},{"P3_8","3.8 Multimedia"},{"P3_9","3.9 Braille Display Use"},{"P3_10","3.10 Braille Tables"},{"P3_11","3.11Customization"},
            {"P4_1","4.1 Performance"},{"P4_2","4.2 Error Recovery"},{"P4_3","4.3 Integration"},{"P4_4","4.4 Accessibility APIs"},{"P4_5","4.5 Settings"},{"P4_6","4.6 Profiles"},{"P4_7","4.7 Support"}
        };

    JPanel dataEntryPanel = new JPanel(new GridBagLayout());
    JPanel view = new JPanel(new BorderLayout());
    view.add(dataEntryPanel, BorderLayout.NORTH);
    view.setBorder(javax.swing.BorderFactory.createEmptyBorder(20,20,20,20));
    JScrollPane scroll = new JScrollPane(view);

    GridBagConstraints gbc = new GridBagConstraints();
    // tighter insets to keep rows within 1-2 lines vertical spacing
    gbc.insets = new Insets(2,2,2,2);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.NORTHWEST; // left-align content
    gbc.weightx = 1.0; // allow fields to take available width

    JLabel title = new JLabel("Screen Reader Skills Progression");
    title.getAccessibleContext().setAccessibleName("Screen Reader Skills Progression Title");
        // explicit title font for LAF-independence
            title.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = GridBagConstraints.REMAINDER;
        dataEntryPanel.add(title, gbc);

    // compute label width using the PhaseScoreField label font (12pt) so wrapping is stable across themes
    java.awt.Font labelFont = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 12);
    String[] labels = java.util.Arrays.stream(parts).map(x->x[1]).toArray(String[]::new);
    int maxPx = com.studentgui.uicomp.PhaseScoreField.computeMaxLabelPixelWidth(labelFont, labels);
    // clamp wider so most labels stay on 1-2 lines (200..360 px)
    com.studentgui.uicomp.PhaseScoreField.setGlobalLabelWidth(Math.min(360, Math.max(200, maxPx + 50)));
    skillFields = new com.studentgui.uicomp.PhaseScoreField[this.parts.length];
        for (int i = 0; i < this.parts.length; i++) {
            gbc.gridy = i + 1;
            gbc.gridwidth = 2;
            gbc.gridx = 0;
            com.studentgui.uicomp.PhaseScoreField f = new com.studentgui.uicomp.PhaseScoreField(this.parts[i][1], 0);
            f.setName("screenreader_" + this.parts[i][0]);
            f.getAccessibleContext().setAccessibleName(this.parts[i][1]);
            f.setToolTipText("Enter a numeric score for " + this.parts[i][1]);
            skillFields[i] = f;
            dataEntryPanel.add(f, gbc);
        }

    // Two side-by-side buttons: Submit Data + Open Latest Plot (match IOS styling)
    gbc.gridy = this.parts.length + 2; gbc.gridx = 0; gbc.gridwidth = GridBagConstraints.REMAINDER; gbc.anchor = GridBagConstraints.WEST;
    javax.swing.JPanel buttonRow = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
    buttonRow.setOpaque(false);

    JButton submit = new JButton("Submit Data");
    submit.setPreferredSize(new java.awt.Dimension(0, 32));
    submit.addActionListener((ActionEvent e) -> { submitData(); refreshGraph(); });
    submit.setMnemonic(KeyEvent.VK_S);
    submit.setToolTipText("Save ScreenReader scores for the selected student (Alt+S)");
    submit.getAccessibleContext().setAccessibleName("Submit ScreenReader Data");
    buttonRow.add(submit);

    JButton openLatest = new JButton("Open Latest Plot");
    openLatest.setPreferredSize(new java.awt.Dimension(0, 32));
    openLatest.addActionListener((ActionEvent e) -> openLatestPlot());
    openLatest.setToolTipText("Open the most recently saved ScreenReader plot for this student");
    buttonRow.add(openLatest);

    dataEntryPanel.add(buttonRow, gbc);
    gbc.gridwidth = 1;

    scroll.getAccessibleContext().setAccessibleName("ScreenReader data entry scroll pane");
    add(scroll, BorderLayout.CENTER);

    SwingUtilities.invokeLater(() -> { view.setPreferredSize(view.getPreferredSize()); scroll.getViewport().setViewPosition(new java.awt.Point(0,0)); revalidate(); });
    // Diagnostic: log spinner positions and actual gap after layout
    SwingUtilities.invokeLater(() -> {
        for (com.studentgui.uicomp.PhaseScoreField f : skillFields) {
            if (f != null) LOG.debug("ScreenReader field {} labelWidth={} spinnerX={} gap={}", f.getLabel(), f.getLabelWrapWidth(), f.getSpinnerX(), f.getActualGap());
        }
    });

        com.studentgui.apphelpers.Helpers.createFolderHierarchy();
        initDatabase();
        refreshGraph();
    }

    /**
     * Ensure the ScreenReader progress type and its assessment parts exist.
     * This is idempotent and safe to call on page creation.
     */
    private void initDatabase() {
        try {
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("ScreenReader");
            String[] codes = new String[]{
                "P1_1","P1_2","P1_3","P1_4","P1_5","P1_6",
                "P2_1","P2_2","P2_3","P2_4",
                "P3_1","P3_2","P3_3","P3_4","P3_5","P3_6","P3_7","P3_8","P3_9","P3_10","P3_11",
                "P4_1","P4_2","P4_3","P4_4","P4_5","P4_6","P4_7"
            };
            com.studentgui.apphelpers.Database.ensureAssessmentParts(ptId, codes);
        } catch (SQLException ex) {
            LOG.error("Error initializing ScreenReader parts", ex);
        }
    }

    /**
     * Collect values from the entry fields, validate them, and persist
     * them to the database as an assessment session.
     */
    private void submitData() {
        if (this.studentNameParam == null || this.studentNameParam.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a student before submitting ScreenReader data.", "Missing student", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int studentId = com.studentgui.apphelpers.Database.getOrCreateStudent(this.studentNameParam);
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("ScreenReader");
            int sessionId = com.studentgui.apphelpers.Database.createProgressSession(studentId, ptId, this.dateParam);
            String[] codes = new String[this.parts.length];
            int[] scores = new int[this.parts.length];
            for (int i = 0; i < this.parts.length; i++) {
                codes[i] = this.parts[i][0];
                scores[i] = skillFields[i].getValue();
            }
            com.studentgui.apphelpers.Database.insertAssessmentResults(sessionId, ptId, codes, scores);
            LOG.info("ScreenReader data submitted for student={}", this.studentNameParam);
            com.studentgui.apphelpers.UiNotifier.show("ScreenReader data saved.");
            com.studentgui.apphelpers.dto.AssessmentPayload payload = new com.studentgui.apphelpers.dto.AssessmentPayload(sessionId, codes, scores);
            java.nio.file.Path jsonOut = com.studentgui.apphelpers.SessionJsonWriter.writeSessionJson(this.studentNameParam, "ScreenReader", payload, sessionId);
            if (jsonOut == null) LOG.warn("Unable to save ScreenReader session JSON for sessionId={}", sessionId);
            try {
                java.nio.file.Path out = com.studentgui.apphelpers.Helpers.APP_HOME.resolve("StudentDataFiles").resolve(com.studentgui.apphelpers.Helpers.safeName(this.studentNameParam)).resolve("plots");
                java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_DATE;
                java.nio.file.Path file = out.resolve("ScreenReader-" + this.dateParam.format(df) + ".png");
                lineGraph.saveChart(file, 800, 400);
                LOG.info("Saved ScreenReader plot to {}", file);
            } catch (java.io.IOException ex) {
                LOG.warn("Unable to save ScreenReader plot image: {}", ex.toString());
            }
        } catch (NumberFormatException ex) {
            LOG.warn("Invalid number in skill fields", ex);
        } catch (SQLException ex) {
            LOG.error("DB error submitting ScreenReader data", ex);
            JOptionPane.showMessageDialog(this, "Database error saving ScreenReader data: " + ex.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Refresh the attached JLineGraph with the latest ScreenReader data for
     * the configured student.
     */
    private void refreshGraph() {
        try {
            List<List<Integer>> allSkillValues = com.studentgui.apphelpers.Database.fetchLatestAssessmentResults(studentNameParam, "ScreenReader", 5);
            if (allSkillValues != null && !allSkillValues.isEmpty()) {
                lineGraph.updateWithData(allSkillValues);
                LOG.info("Graph updated with {} series", allSkillValues.size());
            } else {
                LOG.info("No ScreenReader data to plot for {}", studentNameParam);
            }
        } catch (SQLException ex) {
            LOG.error("Error fetching ScreenReader data", ex);
        }

        // Persist the current chart as a static PNG into the student's plots folder
        try {
            if (this.studentNameParam != null && !this.studentNameParam.trim().isEmpty()) {
                java.nio.file.Path out = com.studentgui.apphelpers.Helpers.APP_HOME.resolve("StudentDataFiles").resolve(com.studentgui.apphelpers.Helpers.safeName(this.studentNameParam)).resolve("plots");
                java.nio.file.Files.createDirectories(out);
                java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_DATE;
                java.nio.file.Path file = out.resolve("ScreenReader-" + (this.dateParam != null ? this.dateParam.format(df) : java.time.LocalDate.now().toString()) + ".png");
                lineGraph.saveChart(file, 800, 400);
                LOG.info("Saved ScreenReader plot to {}", file);
                com.studentgui.apphelpers.UiNotifier.show("ScreenReader plot saved: " + file.getFileName().toString());
            }
        } catch (java.io.IOException ex) {
            LOG.warn("Unable to save ScreenReader plot image: {}", ex.toString());
        }
    }

    private void openLatestPlot() {
        java.nio.file.Path p = com.studentgui.apphelpers.Helpers.latestPlotPath(this.studentNameParam, "ScreenReader");
        if (p == null) {
            com.studentgui.apphelpers.UiNotifier.show("No ScreenReader plot found for student");
            return;
        }
    try { java.awt.Desktop.getDesktop().open(p.toFile()); }
    catch (java.io.IOException | UnsupportedOperationException | SecurityException ex) { com.studentgui.apphelpers.UiNotifier.show("Unable to open plot: " + p.getFileName().toString()); }
    }

}
