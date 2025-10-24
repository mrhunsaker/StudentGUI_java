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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.studentgui.uicomp.PhaseScoreField;

/**
 * Cortical Visual Impairment (CVI) progression page.
 * <p>
 * Presents a collection of named inputs for CVI-related observation scores
 * and supports saving and plotting recent sessions via the shared
 * {@link JLineGraph} component.
 * </p>
 */
public class CVI extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(CVI.class);
    /** Mapping of assessment part codes to their input components. */
    private final Map<String, PhaseScoreField> inputs = new LinkedHashMap<>();

    /** Selected student display name (may be null) used when saving or plotting. */
    private final String studentNameParam;

    /** Session date to associate with saved CVI progress entries. */
    private final LocalDate dateParam;

    /** Shared graph component used to visualize recent CVI results. */
    private final JLineGraph graph;

    /**
     * Construct the CVI page bound to the selected student and session date.
     *
     * @param studentName selected student name (may be null)
     * @param date session date to use when creating progress sessions
     * @param graph shared graph used to visualize recent results
     */
    public CVI(String studentName, LocalDate date, JLineGraph graph) {
        this.studentNameParam = studentName;
        this.dateParam = date;
        this.graph = graph;
    setLayout(new BorderLayout());
    JPanel panel = new JPanel(new GridBagLayout());
    JPanel view = new JPanel(new BorderLayout());
    view.add(panel, BorderLayout.NORTH);
    view.setBorder(javax.swing.BorderFactory.createEmptyBorder(20,20,20,20));
    JScrollPane scroll = new JScrollPane(view);
    GridBagConstraints gbc = new GridBagConstraints(); gbc.insets=new Insets(2,2,2,2); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.NORTHWEST;

    JLabel title = new JLabel("CVI Progression");
    title.setFont(title.getFont().deriveFont(Font.BOLD,16));
    title.getAccessibleContext().setAccessibleName("CVI Progression Title");
    title.setHorizontalAlignment(JLabel.LEFT);
    gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2; panel.add(title, gbc);

    String[][] parts = new String[][]{{"P1_1","Color Preference"},{"P1_2","Need for Movement"},{"P1_3","Latency"},{"P1_4","Field Preference"},{"P1_5","Visual Complexity"},{"P1_6","Nonpurposeful Gaze"},{"P2_1","Distance Viewing"},{"P2_2","Atypical Reflexes"},{"P2_3","Visual Novelty"},{"P2_4","Visual Reach"}};
    String[] labels = java.util.Arrays.stream(parts).map(x->x[1]).toArray(String[]::new);
            int maxPx = com.studentgui.uicomp.PhaseScoreField.computeMaxLabelPixelWidth(title.getFont(), labels);
            com.studentgui.uicomp.PhaseScoreField.setGlobalLabelWidth(Math.min(320, Math.max(140, maxPx + 50)));
    int row = 1;
    for (String[] pdef: parts) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        PhaseScoreField tf = new PhaseScoreField(pdef[1], 0);
        tf.setToolTipText("Enter whole number score for " + pdef[1]);
        tf.getAccessibleContext().setAccessibleName(pdef[1]);
        tf.setName("cvi_" + pdef[0]);
        panel.add(tf, gbc);
        inputs.put(pdef[0], tf);
        row++;
    }

    // Two side-by-side buttons: Submit Data (save + save PNG) and Open Latest Plot
    gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
    JButton submit = new JButton("Submit Data");
    submit.setPreferredSize(new java.awt.Dimension(0, 32));
    submit.addActionListener((ActionEvent e) -> save());
    submit.setToolTipText("Save CVI assessment for selected student (Alt+S)");
    submit.setMnemonic(KeyEvent.VK_S);
    submit.getAccessibleContext().setAccessibleName("Submit CVI Data");
    submit.setName("cvi_submit");
    panel.add(submit, gbc);

    gbc.gridx = 1; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
    JButton openLatest = new JButton("Open Latest Plot");
    openLatest.setPreferredSize(new java.awt.Dimension(0, 32));
    openLatest.addActionListener((ActionEvent e) -> {
        java.nio.file.Path plotPath = com.studentgui.apphelpers.Helpers.latestPlotPath(this.studentNameParam, "CVI");
        if (plotPath == null) com.studentgui.apphelpers.UiNotifier.show("No CVI plot found for student");
        else {
                // Do not auto-open CVI plot on startup; only save it. Opening is handled
                // by explicit user actions (Open Latest Plot).
        }
    });
    panel.add(openLatest, gbc);
    gbc.gridwidth = 1;

        add(scroll, BorderLayout.CENTER); add(graph, BorderLayout.SOUTH);
        SwingUtilities.invokeLater(()->{ panel.setPreferredSize(panel.getPreferredSize()); revalidate(); });
        com.studentgui.apphelpers.Helpers.createFolderHierarchy();
        initParts();
    }

    /**
     * Ensure the CVI progress-type and part rows exist in the database.
     */
    private void initParts() {
        try {
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("CVI");
            java.util.Set<String> keys = inputs.keySet();
            String[] codes = new String[keys.size()];
            int kidx = 0;
            for (String k : keys) codes[kidx++] = k;
            com.studentgui.apphelpers.Database.ensureAssessmentParts(ptId, codes);
        } catch (SQLException ex) {
            LOG.error("Error ensuring CVI parts", ex);
        }
    }

    /**
     * Validate inputs and persist them as a new CVI progress session for the
     * selected student.
     */
    private void save() {
        if (studentNameParam == null || studentNameParam.trim().isEmpty()) {
            com.studentgui.apphelpers.UiNotifier.show("Please select a student before saving CVI data.");
            return;
        }

        try {
            int studentId = com.studentgui.apphelpers.Database.getOrCreateStudent(studentNameParam);
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("CVI");
            int sessionId = com.studentgui.apphelpers.Database.createProgressSession(studentId, ptId, dateParam);
            java.util.Set<String> keys = inputs.keySet();
            String[] codes = new String[keys.size()];
            int kidx = 0;
            for (String k : keys) codes[kidx++] = k;
            int[] scores = new int[codes.length];
            for (int i = 0; i < codes.length; i++) {
                scores[i] = inputs.get(codes[i]).getValue();
            }
            com.studentgui.apphelpers.Database.insertAssessmentResults(sessionId, ptId, codes, scores);
            LOG.info("CVI data saved for {}", studentNameParam);
            com.studentgui.apphelpers.UiNotifier.show("CVI data saved.");
            com.studentgui.apphelpers.dto.AssessmentPayload payload = new com.studentgui.apphelpers.dto.AssessmentPayload(sessionId, codes, scores);
            java.nio.file.Path jsonOut = com.studentgui.apphelpers.SessionJsonWriter.writeSessionJson(this.studentNameParam, "CVI", payload, sessionId);
            if (jsonOut == null) LOG.warn("Unable to save CVI session JSON for sessionId={}", sessionId);
            try {
                java.nio.file.Path out = com.studentgui.apphelpers.Helpers.APP_HOME.resolve("StudentDataFiles").resolve(com.studentgui.apphelpers.Helpers.safeName(this.studentNameParam)).resolve("plots");
                java.nio.file.Files.createDirectories(out);
                java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_DATE;
                String dateStr = this.dateParam != null ? this.dateParam.format(df) : java.time.LocalDate.now().toString();
                String baseName = "CVI-" + sessionId + "-" + dateStr;

                com.studentgui.apphelpers.Database.ResultsWithDates rwd = com.studentgui.apphelpers.Database.fetchLatestAssessmentResultsWithDates(this.studentNameParam, "CVI", Integer.MAX_VALUE);
                java.util.Map<String, java.nio.file.Path> groups = new java.util.LinkedHashMap<>();
                String[] labels = new String[codes.length];
                for (int i = 0; i < codes.length; i++) labels[i] = inputs.get(codes[i]).getLabel();
                if (rwd != null && rwd.rows != null && !rwd.rows.isEmpty()) {
                    graph.updateWithGroupedDataByDate(rwd.dates, rwd.rows, codes, labels);
                    groups = graph.saveGroupedCharts(out, baseName, 1000, 240);
                    java.time.LocalDate headerDate = rwd.dates.get(rwd.dates.size() - 1);
                    dateStr = headerDate.format(df);
                } else {
                    java.util.List<java.util.List<Integer>> rowsList = new java.util.ArrayList<>();
                    java.util.List<Integer> latest = new java.util.ArrayList<>();
                    for (String c : codes) latest.add(inputs.get(c).getValue());
                    rowsList.add(latest);
                    graph.updateWithGroupedData(rowsList, codes);
                    groups = graph.saveGroupedCharts(out, baseName, 1000, 240);
                }

                if (groups == null) groups = new java.util.LinkedHashMap<>();
                StringBuilder md = new StringBuilder();
                md.append("# ").append(this.studentNameParam == null ? "Unknown Student" : this.studentNameParam).append(" - ").append(dateStr).append("\n\n");
                for (java.util.Map.Entry<String, java.nio.file.Path> e : groups.entrySet()) {
                    md.append("## ").append(e.getKey()).append("\n\n");
                    md.append("![](./").append(e.getValue().getFileName().toString()).append(")\n\n");
                }
                java.nio.file.Path mdFile = out.resolve(baseName + ".md");
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
                        html.append("<div class=\"plot\"><img src=\"./").append(imgName).append("\" alt=\"").append(grp).append("\"></div>");
                        java.util.List<Integer> idxs = groupsIdx.getOrDefault(grp, new java.util.ArrayList<>());
                        html.append("<div class=\"legend\">");
                        for (int s = 0; s < idxs.size(); s++) {
                            int idx = idxs.get(s);
                            String code = codes[idx];
                            String human = labels[idx];
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
                    java.nio.file.Path htmlFile = out.resolve(baseName + ".html");
                    java.nio.file.Files.writeString(htmlFile, html.toString(), java.nio.charset.StandardCharsets.UTF_8);
                    LOG.info("Wrote CVI HTML session report {}", htmlFile);
                } catch (java.io.IOException ioex) {
                    LOG.warn("Unable to write CVI HTML report: {}", ioex.toString());
                }
            } catch (java.io.IOException ioe) {
                LOG.warn("Unable to save CVI per-phase plots or markdown report: {}", ioe.toString());
            }
        } catch (SQLException ex) {
            LOG.error("Error saving CVI data", ex);
            com.studentgui.apphelpers.UiNotifier.show("Database error saving CVI data: " + ex.getMessage());
        }
    }

    // Plotting is handled as part of save(): the submit action updates the shared
    // graph and writes a static PNG into the student's plots folder.
}
