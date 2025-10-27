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
 * Digital literacy skills progression page UI.
 * <p>
 * Presents a set of numeric input fields for digital literacy skills and
 * persists entries to the normalized database. A provided {@link JLineGraph}
 * instance is used to visualize recent assessment sessions.
 * </p>
 */
public class DigitalLiteracy extends JPanel implements com.studentgui.app.DateChangeListener, com.studentgui.app.StudentChangeListener {
    private static final Logger LOG = LoggerFactory.getLogger(DigitalLiteracy.class);
    /** Array of input fields for each digital literacy skill part. */
    private final com.studentgui.uicomp.PhaseScoreField[] skillFields;
    /** Canonical list of digital literacy assessment parts: code and display label. */
    private final String[][] parts;

    /** Shared graph used to visualize recent digital literacy sessions. */
    private final JLineGraph lineGraph; // Reference to the JLineGraph instance

    /** Selected student's display name (may be null) for saving/fetching data. */
    private String studentNameParam;
    private JLabel titleLabel;
    private final String baseTitle = "Digital Literacy Skills Progression";

    /** Session date to associate with persisted digital literacy progress. */
    private LocalDate dateParam;

    /**
     * Construct the Digital Literacy page for the given student and date.
     *
     * @param studentName display name of the selected student (may be null)
     * @param date session date to associate with persisted progress
     * @param lineGraph shared graph component used to display recent results
     */
    public DigitalLiteracy(final String studentName, final LocalDate date, final JLineGraph lineGraph) {
    this.studentNameParam = (studentName == null || studentName.trim().isEmpty()) ? com.studentgui.apphelpers.Helpers.defaultStudent() : studentName;
        this.dateParam = date;
        this.lineGraph = lineGraph; // Use the passed in graph instance
        setLayout(new BorderLayout());

    this.parts = new String[][]{
            {"P1_1","1.1 Turn Device On/Off"},{"P1_2","1.2 Turn VoiceOver On/Off"},{"P1_3","1.3 Gestures to Click Icons"},{"P1_4","1.4 Home Screen Icons to Open Documents"},{"P1_5","1.5 Save Documents"},{"P1_6","1.6 Online Tools/Resources"},{"P1_7","1.7 Keyboarding"},{"P1_8","1.8 Use Different Elements"},{"P1_9","1.9 Control Center, App Switcher..."},
            {"P2_1","2.1 Write, edit save"},{"P2_2","2.2 Read, Navigate Document"},{"P2_3","2.3 Use Menubar"},{"P2_4","2.4 Highlight text, copy and paste text"},{"P2_5","2.5 Copy and paste images"},{"P2_6","2.6 Proofread and edit"},
            {"P3_1","3.1 Describe Spreadsheet"},{"P3_2","3.2 Explain terms and concepts"},{"P3_3","3.3 Enter/Edit data"},
            {"P4_1","4.1 Presentation Tools"},{"P4_2","4.2 Create Slides"},{"P4_3","4.3 Edit Slides"},{"P4_4","4.4 Present Slides"},{"P4_5","4.5 Share Slides"},
            {"P5_1","5.1 Acceptable Use"},{"P5_2","5.2 Digital Citizenship"},{"P5_3","5.3 Internet Safety"},{"P5_4","5.4 Copyright"},{"P5_5","5.5 Plagiarism"}
        };

        // Panel for data entry
        JPanel dataEntryPanel = new JPanel();
        dataEntryPanel.setLayout(new GridBagLayout());
        JScrollPane dataEntryScrollPane = new JScrollPane(dataEntryPanel);
        dataEntryScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        dataEntryScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

    this.titleLabel = new JLabel(baseTitle);
        this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        dataEntryPanel.add(this.titleLabel, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.ipady = 20;
        dataEntryPanel.add(new JPanel(), gbc);

    // layout spacing handled by PhaseScoreField

        String[] labels = java.util.Arrays.stream(parts).map(x->x[1]).toArray(String[]::new);
            int maxPx = com.studentgui.uicomp.PhaseScoreField.computeMaxLabelPixelWidth(titleLabel.getFont(), labels);
            com.studentgui.uicomp.PhaseScoreField.setGlobalLabelWidth(Math.min(320, Math.max(140, maxPx + 50)));
    skillFields = new com.studentgui.uicomp.PhaseScoreField[this.parts.length];
    for (int i = 0; i < this.parts.length; i++) {
            gbc.gridy = i + 2;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            com.studentgui.uicomp.PhaseScoreField field = new com.studentgui.uicomp.PhaseScoreField(parts[i][1], 0);
            field.setName("digitalliteracy_" + this.parts[i][0]);
            field.getAccessibleContext().setAccessibleName(this.parts[i][1]);
            field.setToolTipText("Enter whole number score for " + this.parts[i][1]);
            gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(5, 5, 5, 5);
            dataEntryPanel.add(field, gbc);
            skillFields[i] = field;
            gbc.gridx = 2; gbc.gridwidth = 1; gbc.insets = new Insets(5, 0, 5, 5);
            dataEntryPanel.add(new JPanel(), gbc);
        }

    gbc.gridy = this.parts.length + 3;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weighty = 1.0;
        dataEntryPanel.add(new JPanel(), gbc);

    // Place Submit and Open Latest side-by-side and match IOS button height
    gbc.gridy = this.parts.length + 4;
    gbc.weighty = 0.0;
    gbc.gridx = 0;
    gbc.gridwidth = 1;
    JButton submitDataButton = new JButton("Submit Data");
    submitDataButton.setPreferredSize(new java.awt.Dimension(0, 32));
    submitDataButton.addActionListener((ActionEvent e) -> { submitData(); refreshGraph(); });
    submitDataButton.setToolTipText("Save digital literacy scores for the selected student (Alt+S)");
    submitDataButton.setMnemonic(KeyEvent.VK_S);
    submitDataButton.getAccessibleContext().setAccessibleName("Submit Digital Literacy Data");
    submitDataButton.setName("digitalliteracy_submit");
    dataEntryPanel.add(submitDataButton, gbc);

    gbc.gridx = 1;
    JButton openLatestBtn = new JButton("Open Latest Plot");
    openLatestBtn.setPreferredSize(new java.awt.Dimension(0, 32));
    openLatestBtn.addActionListener((ActionEvent e) -> {
        java.nio.file.Path p = com.studentgui.apphelpers.Helpers.latestPlotPath(this.studentNameParam, "DigitalLiteracy");
        if (p == null) {
            com.studentgui.apphelpers.UiNotifier.show("No DigitalLiteracy plot found for student");
        } else {
            try {
                java.awt.Desktop.getDesktop().open(p.toFile());
            } catch (java.io.IOException | UnsupportedOperationException | SecurityException ex) {
                com.studentgui.apphelpers.UiNotifier.show("Unable to open plot: " + p.getFileName().toString());
            }
        }
    });
    dataEntryPanel.add(openLatestBtn, gbc);

    gbc.gridx = 2; gbc.gridwidth = GridBagConstraints.REMAINDER;
    dataEntryPanel.add(new JPanel(), gbc);

    dataEntryScrollPane.getAccessibleContext().setAccessibleName("Digital Literacy data entry scroll pane");

        add(dataEntryScrollPane, BorderLayout.CENTER);

        // Add existing graph reference
        add(lineGraph, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> {
            dataEntryPanel.setPreferredSize(dataEntryPanel.getPreferredSize());
            updateTitleDate();
            revalidate();
        });

        // Ensure application folders and DB schema exist
        com.studentgui.apphelpers.Helpers.createFolderHierarchy();
        initDatabase();
        refreshGraph();
    }

    /**
     * Ensure the progress type and assessment parts for DigitalLiteracy exist
     * in the canonical schema.
     */
    private void initDatabase() {
        try {
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("DigitalLiteracy");
            // Use canonical part codes from this.parts
            String[] codes = new String[this.parts.length];
            for (int i = 0; i < this.parts.length; i++) {
                codes[i] = this.parts[i][0];
            }
            com.studentgui.apphelpers.Database.ensureAssessmentParts(ptId, codes);
        } catch (SQLException e) {
            LOG.error("SQL error ensuring assessment parts for DigitalLiteracy", e);
        }
    }

    /**
     * Validate and persist input field values as a new progress session for
     * the selected student.
     */
    private void submitData() {
        if (studentNameParam == null || studentNameParam.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a student before submitting Digital Literacy data.", "Missing student", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int studentId = com.studentgui.apphelpers.Database.getOrCreateStudent(studentNameParam);
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("DigitalLiteracy");
            int sessionId = com.studentgui.apphelpers.Database.createProgressSession(studentId, ptId, dateParam);

            String[] codes = new String[this.parts.length];
            int[] scores = new int[this.parts.length];
            for (int i = 0; i < this.parts.length; i++) {
                codes[i] = this.parts[i][0];
                scores[i] = skillFields[i].getValue();
            }
            com.studentgui.apphelpers.Database.insertAssessmentResults(sessionId, ptId, codes, scores);
            LOG.info("Data submitted successfully via normalized schema.");
            com.studentgui.apphelpers.UiNotifier.show("Digital Literacy data saved.");
            com.studentgui.apphelpers.dto.AssessmentPayload payload = new com.studentgui.apphelpers.dto.AssessmentPayload(sessionId, codes, scores);
            java.nio.file.Path jsonOut = com.studentgui.apphelpers.SessionJsonWriter.writeSessionJson(this.studentNameParam, "DigitalLiteracy", payload, sessionId);
            if (jsonOut == null) {
                LOG.warn("Unable to save DigitalLiteracy session JSON for sessionId={}", sessionId);
            }
            try {
                java.nio.file.Path plotsOut = com.studentgui.apphelpers.Helpers.studentPlotsDir(this.studentNameParam);
                java.nio.file.Path reportsOut = com.studentgui.apphelpers.Helpers.studentReportsDir(this.studentNameParam);
                java.nio.file.Files.createDirectories(plotsOut);
                java.nio.file.Files.createDirectories(reportsOut);
                java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_DATE;
                String dateStr = this.dateParam != null ? this.dateParam.format(df) : java.time.LocalDate.now().toString();
                String baseName = "DigitalLiteracy-" + sessionId + "-" + dateStr;

                com.studentgui.apphelpers.Database.ResultsWithDates rwd = com.studentgui.apphelpers.Database.fetchLatestAssessmentResultsWithDates(this.studentNameParam, "DigitalLiteracy", Integer.MAX_VALUE);
                java.util.Map<String, java.nio.file.Path> groups = null;
                String[] labels = new String[this.parts.length];
                for (int i = 0; i < this.parts.length; i++) {
                    labels[i] = this.parts[i][1];
                }
                if (rwd != null && rwd.rows != null && !rwd.rows.isEmpty()) {
                    lineGraph.updateWithGroupedDataByDate(rwd.dates, rwd.rows, codes, labels);
                    groups = lineGraph.saveGroupedCharts(plotsOut, baseName, 1000, 240);
                    java.time.LocalDate headerDate = rwd.dates.get(rwd.dates.size() - 1);
                    dateStr = headerDate.format(df);
                } else {
                    java.util.List<java.util.List<Integer>> rowsList = new java.util.ArrayList<>();
                    java.util.List<Integer> latest = new java.util.ArrayList<>();
                    for (int v : scores) latest.add(v);
                    rowsList.add(latest);
                    lineGraph.updateWithGroupedData(rowsList, codes);
                    groups = lineGraph.saveGroupedCharts(plotsOut, baseName, 1000, 240);
                }

                if (groups == null) {
                    groups = new java.util.LinkedHashMap<>();
                }
                StringBuilder md = new StringBuilder();
                md.append("# ").append(this.studentNameParam == null ? "Unknown Student" : this.studentNameParam).append(" - ").append(dateStr).append("\n\n");
                for (java.util.Map.Entry<String, java.nio.file.Path> e : groups.entrySet()) {
                    md.append("## ").append(e.getKey()).append("\n\n");
                    md.append("![](../plots/").append(e.getValue().getFileName().toString()).append(")\n\n");
                }
                java.nio.file.Path mdFile = reportsOut.resolve(baseName + ".md");
                java.nio.file.Files.writeString(mdFile, md.toString(), java.nio.charset.StandardCharsets.UTF_8);

                try {
                    String[] palette = JLineGraph.PALETTE_HEX;
                    java.util.LinkedHashMap<String, java.util.List<Integer>> groupsIdx = new java.util.LinkedHashMap<>();
                    for (int i = 0; i < codes.length; i++) {
                        String code = codes[i];
                        String grp = code != null && code.contains("_") ? code.split("_")[0] : code;
                        groupsIdx.computeIfAbsent(grp, k -> new java.util.ArrayList<>()).add(i);
                    }
                    StringBuilder html = new StringBuilder();
                    html.append("<!doctype html><html><head><meta charset=\"utf-8\"><title>");
                    html.append(this.studentNameParam == null ? "Student Report" : this.studentNameParam).append(" - ").append(dateStr).append("</title>");
                    html.append("<style>body{font-family:sans-serif;margin:20px;} img{max-width:100%;height:auto;border:1px solid #ccc;margin-bottom:8px;} .legend{max-height:160px;overflow:auto;border:1px solid #ddd;padding:8px;margin-bottom:24px;} .legend-item{display:flex;align-items:center;gap:8px;padding:4px 0;} .swatch{width:18px;height:12px;border:1px solid #333;display:inline-block}</style>");
                    html.append("</head><body>");
                    html.append("<h1>").append(this.studentNameParam == null ? "Unknown Student" : this.studentNameParam).append(" - ").append(dateStr).append("</h1>");
                    for (java.util.Map.Entry<String, java.nio.file.Path> e2 : groups.entrySet()) {
                        String grp = e2.getKey();
                        String imgName = e2.getValue().getFileName().toString();
                        html.append("<h2>").append(grp).append("</h2>");
                        html.append("<div class=\"plot\"><img src=\"../plots/").append(imgName).append("\" alt=\"").append(grp).append("\"></div>");
                        java.util.List<Integer> idxs = groupsIdx.getOrDefault(grp, new java.util.ArrayList<>());
                        html.append("<div class=\"legend\">");
                        for (int s = 0; s < idxs.size(); s++) {
                            int idx = idxs.get(s);
                            String code = codes[idx];
                            String human = this.parts[idx][1];
                            String seriesName = code + " - " + human;
                            String color = palette[s % palette.length];
                            html.append("<div class=\"legend-item\">");
                            html.append("<span class=\"swatch\" style=\"background:");
                            html.append(color);
                            html.append(";\"></span>");
                            html.append("<div>");
                            html.append(seriesName);
                            html.append("</div></div>");
                        }
                        html.append("</div>");
                    }
                    html.append("</body></html>");
                    java.nio.file.Path htmlFile = reportsOut.resolve(baseName + ".html");
                    java.nio.file.Files.writeString(htmlFile, html.toString(), java.nio.charset.StandardCharsets.UTF_8);
                    LOG.info("Wrote DigitalLiteracy HTML session report {}", htmlFile);
                } catch (java.io.IOException ioex) {
                    LOG.warn("Unable to write DigitalLiteracy HTML report: {}", ioex.toString());
                }
            } catch (java.io.IOException ioe) {
                LOG.warn("Unable to save DigitalLiteracy per-phase plots or markdown report: {}", ioe.toString());
            }
        } catch (SQLException e) {
            LOG.error("SQL error submitting Digital Literacy data", e);
            JOptionPane.showMessageDialog(this, "Database error saving Digital Literacy data: " + e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Load recent assessment sessions and update the shared {@link JLineGraph}
     * component with the returned values.
     */
    private void refreshGraph() {
        try {
            List<List<Integer>> allSkillValues = com.studentgui.apphelpers.Database.fetchLatestAssessmentResults(studentNameParam, "DigitalLiteracy", 5);
            if (allSkillValues != null && !allSkillValues.isEmpty()) {
                // Build canonical codes array in the same order used when ensuring parts
                String[] codes = new String[this.parts.length];
                for (int i = 0; i < this.parts.length; i++) {
                    codes[i] = this.parts[i][0];
                }
                lineGraph.updateWithGroupedData(allSkillValues, codes);
                LOG.debug("Graph updated with data: {}", allSkillValues);
                // Skip automatic saving of DigitalLiteracy plot during refresh to avoid
                // creating files during application startup.
                LOG.debug("Skipping auto-save of DigitalLiteracy chart during refresh for student={}", this.studentNameParam);
            } else {
                LOG.info("No data to plot.");
            }
        } catch (SQLException e) {
            LOG.error("SQL error refreshing Digital Literacy graph", e);
        }
    }

    @Override
    public void dateChanged(final LocalDate newDate) {
        this.dateParam = newDate;
        SwingUtilities.invokeLater(() -> {
            refreshGraph();
            updateTitleDate();
        });
    }

    @Override
    public void studentChanged(final String newStudent) {
        this.studentNameParam = newStudent;
        SwingUtilities.invokeLater(() -> {
            refreshGraph();
            updateTitleDate();
        });
    }

    private void updateTitleDate() {
        try {
            String dateStr = this.dateParam != null ? this.dateParam.toString() : java.time.LocalDate.now().toString();
            this.titleLabel.setText(baseTitle + " - " + dateStr);
        } catch (Exception ex) {
            this.titleLabel.setText(baseTitle);
        }
    }
    

}
