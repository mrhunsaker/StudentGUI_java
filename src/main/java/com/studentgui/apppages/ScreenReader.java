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
public class ScreenReader extends JPanel implements com.studentgui.app.DateChangeListener, com.studentgui.app.StudentChangeListener {
    private static final Logger LOG = LoggerFactory.getLogger(ScreenReader.class);
    /** Array of input fields corresponding to ScreenReader assessment parts. */
    private final com.studentgui.uicomp.PhaseScoreField[] skillFields;
    /** Canonical parts (code + label) for ScreenReader. */
    private final String[][] parts;

    /** Shared graph component used to visualize recent ScreenReader sessions. */
    private final JLineGraph lineGraph;

    /** Selected student's display name used for saves and plots (may be null). */
    private String studentNameParam;
    /** Title label shown at the top of the page. */
    private JLabel titleLabel;
    /** Base title used for the Screen Reader page header; date is appended when shown. */
    private final String baseTitle = "Screen Reader Skills Progression";

    /** Session date associated with entries made on this page. */
    private LocalDate dateParam;

    /**
     * Construct a ScreenReader page bound to a student and date.
     * The provided JLineGraph is used to render recent assessment results.
     *
     * @param studentName the student display name (may be null to indicate no selection)
     * @param date        the date associated with the session
     * @param lineGraph   chart component used to display recent results
     */
    public ScreenReader(String studentName, LocalDate date, JLineGraph lineGraph) {
    this.studentNameParam = (studentName == null || studentName.trim().isEmpty()) ? com.studentgui.apphelpers.Helpers.defaultStudent() : studentName;
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

    this.titleLabel = new JLabel(baseTitle);
    this.titleLabel.getAccessibleContext().setAccessibleName("Screen Reader Skills Progression Title");
        // explicit title font for LAF-independence
            this.titleLabel.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = GridBagConstraints.REMAINDER;
        dataEntryPanel.add(this.titleLabel, gbc);

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

    gbc.gridy = this.parts.length + 2;
    gbc.weighty = 0.0;
    gbc.gridx = 0;
    gbc.gridwidth = 1;
    gbc.anchor = GridBagConstraints.WEST;
    JButton submit = new JButton("Submit Data");
    submit.setPreferredSize(new java.awt.Dimension(0, 32));
    submit.addActionListener((ActionEvent e) -> { submitData(); refreshGraph(); });
    submit.setMnemonic(KeyEvent.VK_S);
    submit.setToolTipText("Save ScreenReader scores for the selected student (Alt+S)");
    submit.getAccessibleContext().setAccessibleName("Submit ScreenReader Data");
    dataEntryPanel.add(submit, gbc);

    gbc.gridx = 1;
    JButton openLatest = new JButton("Open Latest Plot");
    openLatest.setPreferredSize(new java.awt.Dimension(0, 32));
    openLatest.addActionListener((ActionEvent e) -> openLatestPlot());
    openLatest.setToolTipText("Open the most recently saved ScreenReader plot for this student");
    openLatest.getAccessibleContext().setAccessibleName("Open Latest ScreenReader Plot");
    dataEntryPanel.add(openLatest, gbc);

    // consume remaining columns so layout stays compact and buttons are not clipped
    gbc.gridx = 2; gbc.gridwidth = GridBagConstraints.REMAINDER; gbc.anchor = GridBagConstraints.WEST;
    dataEntryPanel.add(new JPanel(), gbc);

    scroll.getAccessibleContext().setAccessibleName("ScreenReader data entry scroll pane");
    add(scroll, BorderLayout.CENTER);

    SwingUtilities.invokeLater(() -> { view.setPreferredSize(view.getPreferredSize()); scroll.getViewport().setViewPosition(new java.awt.Point(0,0)); updateTitleDate(); revalidate(); });
    // Diagnostic: log spinner positions and actual gap after layout
    SwingUtilities.invokeLater(() -> {
        for (com.studentgui.uicomp.PhaseScoreField f : skillFields) {
            if (f != null) {
                LOG.debug("ScreenReader field {} labelWidth={} spinnerX={} gap={}", f.getLabel(), f.getLabelWrapWidth(), f.getSpinnerX(), f.getActualGap());
            }
        }
    });

        com.studentgui.apphelpers.Helpers.createFolderHierarchy();
        initDatabase();
        // Do not refresh or save graphs automatically on construction to avoid
        // writing files or opening images during application startup.
        // refreshGraph();
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
                if (jsonOut == null) {
                    LOG.warn("Unable to save ScreenReader session JSON for sessionId={}", sessionId);
                }
            try {
                java.nio.file.Path plotsOut = com.studentgui.apphelpers.Helpers.studentPlotsDir(this.studentNameParam);
                java.nio.file.Path reportsOut = com.studentgui.apphelpers.Helpers.studentReportsDir(this.studentNameParam);
                java.nio.file.Files.createDirectories(plotsOut);
                java.nio.file.Files.createDirectories(reportsOut);
                java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_DATE;
                String dateStr = this.dateParam != null ? this.dateParam.format(df) : java.time.LocalDate.now().toString();
                String baseName = "ScreenReader-" + sessionId + "-" + dateStr;

                com.studentgui.apphelpers.Database.ResultsWithDates rwd = com.studentgui.apphelpers.Database.fetchLatestAssessmentResultsWithDates(this.studentNameParam, "ScreenReader", Integer.MAX_VALUE);
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
                    for (int v : scores) {
                        latest.add(v);
                    }
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

                // HTML using shared palette
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
                    LOG.info("Wrote ScreenReader HTML session report {}", htmlFile);
                } catch (java.io.IOException ioex) {
                    LOG.warn("Unable to write ScreenReader HTML report: {}", ioex.toString());
                }

                LOG.info("Wrote ScreenReader session report {} with {} group images", mdFile, groups.size());
            } catch (java.io.IOException | SQLException ex) {
                LOG.warn("Unable to save ScreenReader per-phase plots or markdown report: {}", ex.toString());
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
                String[] codes = new String[this.parts.length];
                    for (int i = 0; i < this.parts.length; i++) {
                        codes[i] = this.parts[i][0];
                    }
                lineGraph.updateWithGroupedData(allSkillValues, codes);
                LOG.info("Graph updated with {} series", allSkillValues.size());
            } else {
                LOG.info("No ScreenReader data to plot for {}", studentNameParam);
            }
        } catch (SQLException ex) {
            LOG.error("Error fetching ScreenReader data", ex);
        }

        // Do not save chart images during refresh to avoid creating files on app startup.
        LOG.debug("Skipping auto-save of ScreenReader chart during refresh for student={}", this.studentNameParam);
    }

    @Override
    public void dateChanged(LocalDate newDate) {
        this.dateParam = newDate;
        SwingUtilities.invokeLater(() -> {
            refreshGraph();
            updateTitleDate();
        });
    }

    @Override
    public void studentChanged(String newStudent) {
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
