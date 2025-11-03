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
public class Abacus extends JPanel implements com.studentgui.app.DateChangeListener, com.studentgui.app.StudentChangeListener {
    private static final Logger LOG = LoggerFactory.getLogger(Abacus.class);

    /** Array of input components for each skill. */
    private final com.studentgui.uicomp.PhaseScoreField[] skillFields;
    /** Canonical list of abacus assessment parts: code and display label. */
    private final String[][] parts;
    /** Shared graph component used to visualize recent results. */
    private final JLineGraph lineGraph; // Reference to the JLineGraph instance
    /** Selected student display name (may be null). */
    private String studentNameParam;
    /** Session date associated with persisted progress. */
    private LocalDate dateParam;
    /**
     * Title label shown at the top of the page.
     */
    private JLabel titleLabel;
    /**
     * Base title text used when rendering the page header (date suffixes are appended).
     */
    private final String baseTitle = "Abacus Skills Progression";

    /**
     * Construct the Abacus page for the given student and session date.
     *
     * @param studentName the selected student's display name (may be null before selection)
     * @param date the date to associate with created progress sessions
     * @param lineGraph the shared graph component used to visualize results
     */
    public Abacus(final String studentName, final LocalDate date, final JLineGraph lineGraph) {
    this.studentNameParam = (studentName == null || studentName.trim().isEmpty()) ? com.studentgui.apphelpers.Helpers.defaultStudent() : studentName;
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

    this.titleLabel = new JLabel(baseTitle);
    this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(Font.BOLD, 16));
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
        if (p == null) {
            com.studentgui.apphelpers.UiNotifier.show("No Abacus plot found for student");
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

        add(dataEntryScrollPane, BorderLayout.CENTER);

        // Add existing graph reference
        add(lineGraph, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> {
            dataEntryPanel.setPreferredSize(dataEntryPanel.getPreferredSize());
            updateTitleDate();
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
            // Use the canonical part codes declared on this page so parts are created
            // with the expected codes like "P1_1", "P1_2", ...
            String[] codes = new String[this.parts.length];
            for (int i = 0; i < this.parts.length; i++) {
                codes[i] = this.parts[i][0];
            }
            com.studentgui.apphelpers.Database.ensureAssessmentParts(ptId, codes);
            try {
                com.studentgui.apphelpers.Database.cleanupAssessmentParts(ptId, codes);
            } catch (SQLException se) {
                LOG.warn("Could not cleanup legacy parts for Abacus", se);
            }
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
            if (jsonOut == null) {
                LOG.warn("Unable to save Abacus session JSON for sessionId={}", sessionId);
            }
            // Generate per-phase PNGs (time-series) and a markdown report for this session
            try {
                java.nio.file.Path plotsOut = com.studentgui.apphelpers.Helpers.studentPlotsDir(this.studentNameParam);
                java.nio.file.Path reportsOut = com.studentgui.apphelpers.Helpers.studentReportsDir(this.studentNameParam);
                java.nio.file.Files.createDirectories(plotsOut);
                java.nio.file.Files.createDirectories(reportsOut);
                java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_DATE;
                String dateStr = (this.dateParam != null ? this.dateParam.format(df) : java.time.LocalDate.now().toString());
                String baseName = "Abacus-" + sessionId + "-" + dateStr;

                // Fetch recent dated sessions (oldest first) to build time-series plots.
                com.studentgui.apphelpers.Database.ResultsWithDates rwd = com.studentgui.apphelpers.Database.fetchLatestAssessmentResultsWithDates(this.studentNameParam, "Abacus", Integer.MAX_VALUE);

                java.util.Map<String, java.nio.file.Path> groups = null;
                if (rwd != null && rwd.rows != null && !rwd.rows.isEmpty()) {
                        // Build human-friendly labels from this.parts and render time-series grouped charts
                        String[] labels = new String[this.parts.length];
                        for (int i = 0; i < this.parts.length; i++) {
                            labels[i] = this.parts[i][1];
                        }
                        lineGraph.updateWithGroupedDataByDate(rwd.dates, rwd.rows, codes, labels);
                    // Persist each group as a PNG (time-series image)
                    groups = lineGraph.saveGroupedCharts(plotsOut, baseName, 1000, 240);
                    // Use the most-recent session date for the report header if available
                    java.time.LocalDate headerDate = rwd.dates.get(rwd.dates.size() - 1);
                    dateStr = headerDate.format(df);
                } else {
                    // Fallback: render only the latest session snapshot
                    java.util.List<java.util.List<Integer>> rows = new java.util.ArrayList<>();
                    java.util.List<Integer> latest = new java.util.ArrayList<>();
                    for (int v : scores) {
                        latest.add(v);
                    }
                    rows.add(latest);
                    lineGraph.updateWithGroupedData(rows, codes);
                    groups = lineGraph.saveGroupedCharts(plotsOut, baseName, 1000, 240);
                }

                // Generate markdown report
                if (groups == null) {
                    groups = new java.util.LinkedHashMap<>();
                }
                StringBuilder md = new StringBuilder();
                md.append("# ").append(this.studentNameParam == null ? "Unknown Student" : this.studentNameParam).append(" - ").append(dateStr).append("\n\n");
                for (java.util.Map.Entry<String, java.nio.file.Path> e : groups.entrySet()) {
                    md.append("## ").append(e.getKey()).append("\n\n");
                    md.append("![](./").append(e.getValue().getFileName().toString()).append(")\n\n");
                }
                java.nio.file.Path mdFile = reportsOut.resolve(baseName + ".md");
                // images live in ../plots relative to reports
                String mdText = md.toString().replace("![](./", "![](../plots/");
                java.nio.file.Files.writeString(mdFile, mdText, java.nio.charset.StandardCharsets.UTF_8);
                LOG.info("Wrote Abacus session report {} with {} group images", mdFile, groups.size());
                // Also produce a simple HTML report that embeds the PNGs and
                // shows a scrollable legend under each plot.
                try {
                    String[] palette = new String[] {"#1b9e77","#d95f02","#7570b3","#e7298a","#66a61e","#e6ab02","#a6761d","#666666"};

                    // Build a map of group -> list of part indexes to recreate legend order
                    java.util.LinkedHashMap<String, java.util.List<Integer>> groupsIdx = new java.util.LinkedHashMap<>();
                    for (int i = 0; i < codes.length; i++) {
                        String code = codes[i];
                        String grp = code != null && code.contains("_") ? code.split("_")[0] : code;
                        groupsIdx.computeIfAbsent(grp, k -> new java.util.ArrayList<>()).add(i);
                    }

                    StringBuilder html = new StringBuilder();
                    html.append("<!doctype html>\n<html><head><meta charset=\"utf-8\"><title>");
                    html.append(this.studentNameParam == null ? "Student Report" : this.studentNameParam).append(" - ").append(dateStr);
                    html.append("</title>");
                    html.append("<style>body{font-family:sans-serif;margin:20px;} img{max-width:100%;height:auto;border:1px solid #ccc;margin-bottom:8px;} .legend{max-height:160px;overflow:auto;border:1px solid #ddd;padding:8px;margin-bottom:24px;} .legend-item{display:flex;align-items:center;gap:8px;padding:4px 0;} .swatch{width:18px;height:12px;border:1px solid #333;display:inline-block}</style>");
                    html.append("</head><body>");
                    html.append("<h1>").append(this.studentNameParam == null ? "Unknown Student" : this.studentNameParam).append(" - ").append(dateStr).append("</h1>");

                    for (java.util.Map.Entry<String, java.nio.file.Path> e2 : groups.entrySet()) {
                        String grp = e2.getKey();
                        String imgName = e2.getValue().getFileName().toString();
                        html.append("<h2>").append(grp).append("</h2>");
                        html.append("<div class=\"plot\"><img src=\"./").append(imgName).append("\" alt=\"").append(grp).append("\"></div>");

                        // legend for this group
                        java.util.List<Integer> idxs = groupsIdx.getOrDefault(grp, new java.util.ArrayList<>());
                        html.append("<div class=\"legend\">");
                        for (int s = 0; s < idxs.size(); s++) {
                            int idx = idxs.get(s);
                            String code = codes[idx];
                            String human = this.parts[idx][1];
                            String seriesName = code + " - " + human;
                            String color = palette[s % palette.length];
                            html.append("<div class=\"legend-item\"><span class=\"swatch\" style=\"background:" + color + ";\"></span>");
                            html.append("<div>").append(seriesName).append("</div></div>");
                        }
                        html.append("</div>");
                    }

                    html.append("</body></html>");
                    java.nio.file.Path htmlFile = reportsOut.resolve(baseName + ".html");
                    // adjust image src to point to ../plots
                    String htmlStr = html.toString().replace("src=\"./", "src=\"../plots/");
                    java.nio.file.Files.writeString(htmlFile, htmlStr, java.nio.charset.StandardCharsets.UTF_8);
                    LOG.info("Wrote Abacus HTML session report {}", htmlFile);
                } catch (java.io.IOException ioex) {
                    LOG.warn("Unable to write HTML report: {}", ioex.toString());
                }
            } catch (java.io.IOException | SQLException ex) {
                LOG.warn("Unable to save Abacus per-phase plots or markdown report: {}", ex.toString());
            }
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
            com.studentgui.apphelpers.Database.ResultsWithDates rwd = com.studentgui.apphelpers.Database.fetchLatestAssessmentResultsWithDates(studentNameParam, "Abacus", Integer.MAX_VALUE);
            String[] codes = new String[this.parts.length];
            for (int i = 0; i < this.parts.length; i++) {
                codes[i] = this.parts[i][0];
            }
            if (rwd != null && rwd.rows != null && !rwd.rows.isEmpty()) {
                // Use the date-aware grouped plotter so X axis is dates and each
                // skill within a phase is a separate line series.
                String[] labels = new String[this.parts.length];
                for (int i = 0; i < this.parts.length; i++) {
                    labels[i] = this.parts[i][1];
                }
                lineGraph.updateWithGroupedDataByDate(rwd.dates, rwd.rows, codes, labels);
                LOG.debug("Graph updated with {} dated sessions", rwd.rows.size());
            } else {
                LOG.info("No data to plot; showing grouped placeholders.");
                lineGraph.showEmptyGrouped(codes);
            }
        } catch (SQLException e) {
            LOG.error("SQL error refreshing graph", e);
        }
    }
    @Override
    public void dateChanged(final LocalDate newDate) {
        this.dateParam = newDate;
        // When the global date changes, update the graph to reflect any
        // date-related logic (most refreshGraph implementations load
        // recent sessions independent of the selected session date, but
        // updating here keeps the saved date in sync for future submits).
        SwingUtilities.invokeLater(this::refreshGraph);
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
