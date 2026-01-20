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
 * iOS and iPadOS assistive technology proficiency assessment page.
 *
 * <p>Provides structured evaluation of iOS/iPadOS device usage skills across
 * 41 competencies organized into 6 functional domains:</p>
 *
 * <ul>
 *   <li><b>Phase 1 (P1_1–P1_9): Device Basics and VoiceOver Fundamentals</b>
 *     <ul>
 *       <li>Power management, VoiceOver activation/deactivation</li>
 *       <li>Core gestures (tap, swipe, rotor) for icon navigation and interaction</li>
 *       <li>Home screen management, document handling, keyboarding basics</li>
 *       <li>Control Center, App Switcher, and system-level navigation</li>
 *     </ul>
 *   </li>
 *   <li><b>Phase 2 (P2_1–P2_6): Word Processing and Document Creation</b>
 *     <ul>
 *       <li>Creating, editing, and saving text documents</li>
 *       <li>Reading and navigating within documents using VoiceOver</li>
 *       <li>Menu bar interaction, text/image copy-paste workflows</li>
 *       <li>Proofreading and editing strategies with assistive technology</li>
 *     </ul>
 *   </li>
 *   <li><b>Phase 3 (P3_1–P3_5): Spreadsheet and Data Visualization</b>
 *     <ul>
 *       <li>Spreadsheet concepts and terminology (rows, columns, cells, formulas)</li>
 *       <li>Data entry, editing, and spreadsheet navigation with VoiceOver</li>
 *       <li>Creating and interpreting charts/graphs from data</li>
 *     </ul>
 *   </li>
 *   <li><b>Phase 4 (P4_1–P4_5): Presentation Software</b>
 *     <ul>
 *       <li>Creating and structuring presentations with accessible workflows</li>
 *       <li>Editing slides, adding multimedia content (images, audio)</li>
 *       <li>Presenting slides effectively using assistive technology</li>
 *       <li>Sharing and exporting presentations</li>
 *     </ul>
 *   </li>
 *   <li><b>Phase 5 (P5_1–P5_7): Digital Citizenship and Online Safety</b>
 *     <ul>
 *       <li>Acceptable Use Policies, digital citizenship principles</li>
 *       <li>Online safety, privacy awareness, copyright/plagiarism concepts</li>
 *       <li>Recognizing and responding to cyberbullying</li>
 *     </ul>
 *   </li>
 *   <li><b>Phase 6 (P6_1–P6_11): Device Management and Connectivity</b>
 *     <ul>
 *       <li>App installation, updates, deletion, storage management</li>
 *       <li>Accessibility settings configuration and customization</li>
 *       <li>Screen Time controls, Parental Controls</li>
 *       <li>Connectivity features: Bluetooth, Wi-Fi, AirDrop, Personal Hotspot</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><b>Data Management and Artifacts:</b></p>
 * <ul>
 *   <li>Scores captured via {@link PhaseScoreField} components (typically 0–4 integer range)</li>
 *   <li>Persisted to normalized schema using {@link com.studentgui.apphelpers.Database#insertAssessmentResults}</li>
 *   <li>JSON session export: {@code StudentDataFiles/<student>/Sessions/iOS/iOS-<sessionId>-<timestamp>.json}</li>
 *   <li>Phase-grouped time-series PNG plots saved to {@code plots/} directory</li>
 *   <li>Markdown and HTML reports generated with embedded plots and color-coded legends</li>
 * </ul>
 *
 * <p>The shared {@link JLineGraph} visualizes recent session trends grouped by phase prefix
 * to maintain chart readability. This page operates on static student/date parameters and
 * does not implement listener interfaces for dynamic updates.</p>
 *
 * @see com.studentgui.apphelpers.Database
 * @see JLineGraph
 * @see PhaseScoreField
 */
public class IOS extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(IOS.class);
    /** Mapping of iOS assessment part codes to their input components. */
    private final Map<String, PhaseScoreField> inputs = new LinkedHashMap<>();

    /** Selected student display name used for saves and plots (may be null). */
    private final String studentNameParam;

    /** Session date to associate with saved iOS progress entries. */
    private final LocalDate dateParam;

    /** Shared graph component for plotting recent iOS assessment sessions. */
    private final JLineGraph graph;

    /**
     * Construct the iOS page for the given student and date.
     *
     * @param studentName selected student name (may be null)
     * @param date session date to associate with saved progress
     * @param graph shared graph used to visualize recent sessions
     */
    public IOS(String studentName, LocalDate date, JLineGraph graph) {
    this.studentNameParam = (studentName == null || studentName.trim().isEmpty()) ? com.studentgui.apphelpers.Helpers.defaultStudent() : studentName;
        this.dateParam = date;
        this.graph = graph;
        setLayout(new BorderLayout());

    JPanel p = new JPanel(new GridBagLayout());
    JPanel view = new JPanel(new BorderLayout());
    view.add(p, BorderLayout.NORTH);
    view.setBorder(javax.swing.BorderFactory.createEmptyBorder(20,20,20,20));
    JScrollPane scroll = new JScrollPane(view);
    scroll.getAccessibleContext().setAccessibleName("iOS data entry scroll pane");
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets=new Insets(2,2,2,2); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.NORTHWEST; gbc.weightx = 1.0;

    JLabel title = new JLabel("iOS / iPad OS Skills");
    title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
    title.getAccessibleContext().setAccessibleName("iOS Skills Title");
    title.setHorizontalAlignment(JLabel.LEFT);
    gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2; p.add(title, gbc);

        String[][] parts = new String[][]{
            {"P1_1","1.1 Turn Device On/Off"},{"P1_2","1.2 Turn VoiceOver On/Off"},{"P1_3","1.3 Gestures to Click Icons"},
            {"P1_4","1.4 Home Screen Icons to Open Documents"},{"P1_5","1.5 Save Documents"},{"P1_6","1.6 Online Tools/Resources"},
            {"P1_7","1.7 Keyboarding"},{"P1_8","1.8 Use Different Elements"},{"P1_9","1.9 Control Center, App Switcher..."},
            {"P2_1","2.1 Write, edit save"},{"P2_2","2.2 Read, Navigate Document"},{"P2_3","2.3 Use Menubar"},
            {"P2_4","2.4 Highlight text, copy and paste text"},{"P2_5","2.5 Copy and paste images"},{"P2_6","2.6 Proofread and edit"},
            {"P3_1","3.1 Describe Spreadsheet"},{"P3_2","3.2 Explain terms and concepts"},{"P3_3","3.3 Enter/Edit data"},
            {"P3_4","3.4 Navigate Spreadsheet"},{"P3_5","3.5 Create Graphs"},{"P4_1","4.1 Create Presentation"},
            {"P4_2","4.2 Edit Slides"},{"P4_3","4.3 Add Images"},{"P4_4","4.4 Present Slides"},{"P4_5","4.5 Share Presentation"},
            {"P5_1","5.1 Acceptable Use Policy"},{"P5_2","5.2 Digital Citizenship"},{"P5_3","5.3 Online Safety"},
            {"P5_4","5.4 Copyright"},{"P5_5","5.5 Plagiarism"},{"P5_6","5.6 Privacy"},{"P5_7","5.7 Cyberbullying"},
            {"P6_1","6.1 Install Apps"},{"P6_2","6.2 Update Apps"},{"P6_3","6.3Delete Apps"},{"P6_4","6.4 Manage Storage"},
            {"P6_5","6.5 Accessibility Settings"},{"P6_6","6.6 Screen Time"},{"P6_7","6.7 Parental Controls"},{"P6_8","6.8 Bluetooth"},
            {"P6_9","6.9 Wi-Fi"},{"P6_10","6.10 AirDrop"},{"P6_11","6.11 Hotspot"}
        };

    java.awt.Font labelFont = com.studentgui.uicomp.PhaseScoreField.getLabelFont();
    String[] labels = java.util.Arrays.stream(parts).map(x->x[1]).toArray(String[]::new);
        int maxPx = com.studentgui.uicomp.PhaseScoreField.computeMaxLabelPixelWidth(labelFont, labels);
        com.studentgui.uicomp.PhaseScoreField.setGlobalLabelWidth(Math.min(360, Math.max(200, maxPx + 50)));
    int row = 1;
        for (String[] part : parts) {
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
            PhaseScoreField tf = new PhaseScoreField(part[1], 0);
            tf.setToolTipText("Enter whole number score for " + part[1]);
            tf.getAccessibleContext().setAccessibleName(part[1]);
            tf.setName("ios_" + part[0]);
            p.add(tf, gbc);
            inputs.put(part[0], tf);
            row++;
        }
    // Place Save and Open Latest side-by-side (Braille style)
    gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
    JButton save = new JButton("Save iOS Data");
    save.setPreferredSize(new java.awt.Dimension(0, 32));
    save.addActionListener((ActionEvent e) -> { save(); plot(); });
    save.setToolTipText("Save iOS assessment for selected student");
    save.setMnemonic(KeyEvent.VK_S);
    save.getAccessibleContext().setAccessibleName("Save iOS Data");
    p.add(save, gbc);

    gbc.gridx = 1;
    JButton openLatest = new JButton("Open Latest Plot");
    openLatest.setPreferredSize(new java.awt.Dimension(0, 32));
    openLatest.addActionListener((ActionEvent e) -> {
        java.nio.file.Path pth = com.studentgui.apphelpers.Helpers.latestPlotPath(this.studentNameParam, "iOS");
        if (pth == null) {
            com.studentgui.apphelpers.UiNotifier.show("No iOS plot found for student");
        } else {
            try {
                java.awt.Desktop.getDesktop().open(pth.toFile());
            } catch (java.io.IOException | UnsupportedOperationException | SecurityException ex) {
                com.studentgui.apphelpers.UiNotifier.show("Unable to open plot: " + pth.getFileName().toString());
            }
        }
    });
    p.add(openLatest, gbc);

    // consume remaining columns (if any) so layout stays compact and buttons are not clipped
    gbc.gridx = 2; gbc.gridwidth = GridBagConstraints.REMAINDER; gbc.anchor = GridBagConstraints.WEST;
    p.add(new JPanel(), gbc);
    row++;

        add(scroll, BorderLayout.CENTER);
        add(graph, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(()->{
            view.setPreferredSize(view.getPreferredSize());
            scroll.getViewport().setViewPosition(new java.awt.Point(0,0));
            revalidate();
        });

        SwingUtilities.invokeLater(() -> {
            for (var f: inputs.values()) LOG.debug("IOS field {} labelWidth={} spinnerX={} gap={}", f.getLabel(), f.getLabelWrapWidth(), f.getSpinnerX(), f.getActualGap());
        });

        com.studentgui.apphelpers.Helpers.createFolderHierarchy();
        initParts();
    }

    /**
     * Ensure the iOS progress-type and part rows exist in the normalized
     * database schema.
     */
    private void initParts() {
        try {
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("iOS");
                java.util.Set<String> keys = inputs.keySet();
            String[] codes = new String[keys.size()];
            int idx = 0;
            for (String k : keys) {
                codes[idx++] = k;
            }
            com.studentgui.apphelpers.Database.ensureAssessmentParts(ptId, codes);
        } catch (SQLException ex) {
            LOG.error("Error ensuring iOS assessment parts", ex);
        }
    }

    /**
     * Validate inputs and persist them as a new progress session for the
     * selected student.
     */
    private void save() {
        if (this.studentNameParam == null || this.studentNameParam.trim().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Please select a student before saving iOS data.", "Missing student", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int studentId = com.studentgui.apphelpers.Database.getOrCreateStudent(this.studentNameParam);
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("iOS");
            int sessionId = com.studentgui.apphelpers.Database.createProgressSession(studentId, ptId, this.dateParam);
            java.util.Set<String> keys = inputs.keySet();
            String[] codes = new String[keys.size()]; int idx = 0; for (String k: keys) codes[idx++] = k;
            int[] scores = new int[codes.length];
            for (int i = 0; i < codes.length; i++) {
                scores[i] = inputs.get(codes[i]).getValue();
            }
            com.studentgui.apphelpers.Database.insertAssessmentResults(sessionId, ptId, codes, scores);
            LOG.info("iOS data saved for {}", this.studentNameParam);
            com.studentgui.apphelpers.UiNotifier.show("iOS data saved.");
            com.studentgui.apphelpers.dto.AssessmentPayload payload = new com.studentgui.apphelpers.dto.AssessmentPayload(sessionId, codes, scores);
            java.nio.file.Path jsonOut = com.studentgui.apphelpers.SessionJsonWriter.writeSessionJson(this.studentNameParam, "iOS", payload, sessionId);
            if (jsonOut == null) LOG.warn("Unable to save iOS session JSON for sessionId={}", sessionId);
            try {
                java.nio.file.Path plotsOut = com.studentgui.apphelpers.Helpers.studentPlotsDir(this.studentNameParam);
                java.nio.file.Path reportsOut = com.studentgui.apphelpers.Helpers.studentReportsDir(this.studentNameParam);
                java.nio.file.Files.createDirectories(plotsOut);
                java.nio.file.Files.createDirectories(reportsOut);
                java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_DATE;
                String dateStr = this.dateParam != null ? this.dateParam.format(df) : java.time.LocalDate.now().toString();
                String baseName = "iOS-" + sessionId + "-" + dateStr;

                com.studentgui.apphelpers.Database.ResultsWithDates rwd = com.studentgui.apphelpers.Database.fetchLatestAssessmentResultsWithDates(this.studentNameParam, "iOS", Integer.MAX_VALUE);
                    java.util.Map<String, java.nio.file.Path> groups = null;
                        String[] labels = new String[codes.length];
                        for (int i = 0; i < codes.length; i++) {
                            labels[i] = inputs.get(codes[i]).getLabel();
                        }
                // codes already built above as 'codes'
                if (rwd != null && rwd.rows != null && !rwd.rows.isEmpty()) {
                    graph.updateWithGroupedDataByDate(rwd.dates, rwd.rows, codes, labels);
                    groups = graph.saveGroupedCharts(plotsOut, baseName, 1000, 240);
                    java.time.LocalDate headerDate = rwd.dates.get(rwd.dates.size() - 1);
                    dateStr = headerDate.format(df);
                } else {
                    java.util.List<java.util.List<Integer>> rowsList = new java.util.ArrayList<>();
                        java.util.List<Integer> latest = new java.util.ArrayList<>();
                        for (String c : codes) {
                            latest.add(inputs.get(c).getValue());
                        }
                        rowsList.add(latest);
                    graph.updateWithGroupedData(rowsList, codes);
                    groups = graph.saveGroupedCharts(plotsOut, baseName, 1000, 240);
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
                            int itemIdx = idxs.get(s);
                            String code = codes[itemIdx];
                            String human = labels[itemIdx];
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
                    LOG.info("Wrote iOS HTML session report {}", htmlFile);
                } catch (java.io.IOException ioex) {
                    LOG.warn("Unable to write iOS HTML report: {}", ioex.toString());
                }
            } catch (java.io.IOException ioe) {
                LOG.warn("Unable to save iOS per-phase plots or markdown report: {}", ioe.toString());
            }
        } catch (SQLException ex) {
            LOG.error("Error saving iOS data", ex);
            javax.swing.JOptionPane.showMessageDialog(this, "Database error saving iOS data: " + ex.getMessage(), "Database error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Fetch recent iOS assessment sessions and update the shared graph view.
     */
    private void plot() {
        LOG.info("Plot requested for {}", studentNameParam);
        try {
            java.util.List<java.util.List<Integer>> data = com.studentgui.apphelpers.Database.fetchLatestAssessmentResults(this.studentNameParam, "iOS", 20);
            if (data != null && !data.isEmpty()) {
                // Build codes array in the same order as inputs were created
                String[] codes = new String[inputs.size()];
                int idx = 0; for (String k: inputs.keySet()) codes[idx++] = k;
                graph.updateWithGroupedData(data, codes);
                // Save static PNG
                if (this.studentNameParam != null && !this.studentNameParam.trim().isEmpty()) {
                    try {
                        java.nio.file.Path out = com.studentgui.apphelpers.Helpers.APP_HOME.resolve("StudentDataFiles").resolve(com.studentgui.apphelpers.Helpers.safeName(this.studentNameParam)).resolve("plots");
                        java.nio.file.Files.createDirectories(out);
                        java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_DATE;
                        String dateStr = (this.dateParam != null ? this.dateParam.format(df) : java.time.LocalDate.now().toString());
                        java.nio.file.Path file = out.resolve("iOS-" + dateStr + ".png");
                        graph.saveChart(file, 800, 400);
                        LOG.info("Saved iOS plot to {}", file);
                        // Do not auto-open the plot here; only save it. Opening is handled
                        // by submit/save handlers or the Open Latest button.
                        com.studentgui.apphelpers.UiNotifier.show("iOS plot saved to " + file.toString());
                    } catch (java.io.IOException ex) { LOG.warn("Unable to save iOS plot image: {}", ex.toString()); }
                }
            } else {
                LOG.info("No iOS data to plot for {}", studentNameParam);
            }
        } catch (SQLException ex) {
            LOG.error("Error fetching iOS data for plot", ex);
        }
    }
}
