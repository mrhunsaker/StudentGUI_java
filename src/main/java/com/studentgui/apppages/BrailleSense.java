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
 * BrailleSense skills progression UI page.
 * <p>
 * Presents a compact set of inputs keyed by part code (e.g. P1_1) and allows
 * saving those values into the canonical database schema. A shared
 * {@link JLineGraph} instance is used to visualize recent results.
 * </p>
 */
public class BrailleSense extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(BrailleSense.class);
    /** Map of assessment part codes to their input components. */
    private final Map<String, PhaseScoreField> inputs = new LinkedHashMap<>();
    /** Canonical assessment parts for BrailleSense. */
    private final String[][] parts;
    /** Selected student display name (may be null). */
    private final String studentNameParam;
    /** Date associated with the current session. */
    private final LocalDate dateParam;
    /** Shared graph component used to visualize recent results. */
    private final JLineGraph graph;

    /**
     * Create a BrailleSense page bound to the provided student and date.
     *
     * @param studentName selected student name (may be null until selection)
     * @param date session date to associate with persisted progress rows
     * @param graph shared graph component used to plot recent results
     */
    public BrailleSense(String studentName, LocalDate date, JLineGraph graph) {
        this.studentNameParam = studentName;
        this.dateParam = date;
        this.graph = graph;
        setLayout(new BorderLayout());

        // create a data entry panel that mirrors BrailleNote's layout so alignment is identical
        JPanel dataEntryPanel = new JPanel(new GridBagLayout());
        JPanel view = new JPanel(new BorderLayout());
        view.add(dataEntryPanel, BorderLayout.NORTH);
        view.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JScrollPane dataEntryScrollPane = new JScrollPane(view);
        dataEntryScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        dataEntryScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        dataEntryScrollPane.getAccessibleContext().setAccessibleName("BrailleSense data entry scroll pane");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        JLabel titleLabel = new JLabel("BrailleSense Skills");
        // Use an explicit font so theme changes don't alter the title appearance
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        dataEntryPanel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.ipady = 20;
        dataEntryPanel.add(new JPanel(), gbc);

        this.parts = new String[][]{
                {"P1_1", "1.1 Physical Layout"}, {"P1_2", "1.2 Setup/Universal Commands"}, {"P1_3", "1.3 BNT+ Navigation"}, {"P1_4", "1.4 File Management"}, {"P1_5", "1.5 Word Processor"}, {"P1_6", "1.6 Email"}, {"P1_7", "1.7 Internet"}, {"P1_8", "1.8 Calculator"}, {"P1_9", "1.9 KeyMath"},
                {"P2_1", "2.1 Calendar"}, {"P2_2", "2.2 KeyBRF"}, {"P2_3", "2.3 KeyFiles"}, {"P2_4", "2.4 KeyMail"}, {"P2_5", "2.5 KeyWeb"}, {"P2_6", "2.6 KeyCalc"}, {"P2_7", "2.7 KeyWord"},
                {"P3_1", "3.1 KeySlides"}, {"P3_2", "3.2 KeyCode"}, {"P3_3", "3.3 Third Party Apps"}, {"P3_4", "3.4 Braille Input"}, {"P3_5", "3.5 Braille Output"}, {"P3_6", "3.6 Settings"}, {"P3_7", "3.7 Accessibility"},
                {"P4_1", "4.1 Advanced File Management"}, {"P4_2", "4.2 Cloud Integration"}, {"P4_3", "4.3 Device Maintenance"},
                {"P5_1", "5.1 Collaboration"}, {"P5_2", "5.2 Export/Import"}, {"P5_3", "5.3 Printing"}, {"P5_4", "5.4 Backup"},
                {"P6_1", "6.1 App Installation"}, {"P6_2", "6.2 App Updates"}, {"P6_3", "6.3 Troubleshooting"},
                {"P7_1", "7.1 Custom Shortcuts"}, {"P7_2", "7.2 Macros"}, {"P7_3", "7.3 Scripting"}, {"P7_4", "7.4 Automation"},
                {"P8_1", "8.1 Bluetooth Devices"}, {"P8_2", "8.2 USB Devices"}, {"P8_3", "8.3 External Displays"}, {"P8_4", "8.4 Audio Output"}, {"P8_5", "8.5 Video Output"},
                {"P9_1", "9.1 Security"}, {"P9_2", "9.2 User Accounts"}, {"P9_3", "9.3 Parental Controls"}, {"P9_4", "9.4 Network Settings"},
                {"P10_1", "10.1 Speech Settings"}, {"P10_2", "10.2 Voice Profiles"}, {"P10_3", "10.3 Language Support"},
                {"P11_1", "11.1 Firmware Updates"}, {"P11_2", "11.2 Diagnostics"}, {"P11_3", "11.3 Logs"}, {"P11_4", "11.4 Support"}, {"P11_5", "11.5 Warranty"},
                {"P12_1", "12.1 Community Resources"}, {"P12_2", "12.2 Online Help"}, {"P12_3", "12.3 User Forums"}, {"P12_4", "12.4 Feedback"}
        };

        // compute pixel width using font metrics so labels align precisely
        String[] labels = java.util.Arrays.stream(this.parts).map(x -> x[1]).toArray(String[]::new);
        int maxPx = com.studentgui.uicomp.PhaseScoreField.computeMaxLabelPixelWidth(titleLabel.getFont(), labels);
        com.studentgui.uicomp.PhaseScoreField.setGlobalLabelWidth(Math.min(360, Math.max(200, maxPx + 50)));
        int row = 1;
        for (String[] def : this.parts) {
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            PhaseScoreField tf = new PhaseScoreField(def[1], 0);
            tf.setName("braillesense_" + def[0]);
            tf.getAccessibleContext().setAccessibleName(def[1]);
            tf.setToolTipText("Enter score for " + def[1]);
            dataEntryPanel.add(tf, gbc);
            inputs.put(def[0], tf);
            row++;
        }

        // Place Submit and Open Latest side-by-side to match IOS/ScreenReader styling
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JButton submit = new JButton("Submit Data");
        submit.setPreferredSize(new java.awt.Dimension(0, 32));
        submit.addActionListener((ActionEvent e) -> save());
        submit.setMnemonic(KeyEvent.VK_S);
        submit.setToolTipText("Save BrailleSense scores (Alt+S)");
        submit.getAccessibleContext().setAccessibleName("Submit BrailleSense Data");
        submit.setName("braillesense_submit");
        dataEntryPanel.add(submit, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JButton openLatest = new JButton("Open Latest Plot");
        openLatest.setPreferredSize(new java.awt.Dimension(0, 32));
        openLatest.addActionListener((ActionEvent e) -> {
            java.nio.file.Path p = com.studentgui.apphelpers.Helpers.latestPlotPath(this.studentNameParam, "BrailleSense");
            if (p == null) {
                com.studentgui.apphelpers.UiNotifier.show("No BrailleSense plot found for student");
            } else {
                try {
                    java.awt.Desktop.getDesktop().open(p.toFile());
                } catch (java.io.IOException | UnsupportedOperationException | SecurityException ex) {
                    com.studentgui.apphelpers.UiNotifier.show("Unable to open plot: " + p.getFileName().toString());
                }
            }
        });
        dataEntryPanel.add(openLatest, gbc);

        // Filler to consume remaining horizontal space
        gbc.gridx = 2;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;
        dataEntryPanel.add(new JPanel(), gbc);

        dataEntryScrollPane.getAccessibleContext().setAccessibleName("BrailleSense data entry scroll pane");
        add(dataEntryScrollPane, BorderLayout.CENTER);
        add(graph, BorderLayout.SOUTH);
        SwingUtilities.invokeLater(() -> {
            dataEntryPanel.setPreferredSize(dataEntryPanel.getPreferredSize());
            revalidate();
        });
        SwingUtilities.invokeLater(() -> {
            for (var e : inputs.values()) {
                LOG.debug("BrailleSense field {} labelWidth={} spinnerX={} gap={}", e.getLabel(), e.getLabelWrapWidth(), e.getSpinnerX(), e.getActualGap());
            }
        });
        com.studentgui.apphelpers.Helpers.createFolderHierarchy();
        initParts();
    }

    /**
     * Ensure the database contains the progress-type and assessment part rows
     * for BrailleSense. Safe to call repeatedly.
     */
    private void initParts() {
        try {
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("BrailleSense");
            String[] codes = inputs.keySet().toArray(String[]::new);
            com.studentgui.apphelpers.Database.ensureAssessmentParts(ptId, codes);
        } catch (SQLException ex) {
            LOG.error("Error ensuring braillesense parts", ex);
        }
    }

    /**
     * Persist the current inputs as a new progress session for the selected
     * student. Non-integer input is treated as zero.
     */
    private void save() {
        try {
            int studentId = com.studentgui.apphelpers.Database.getOrCreateStudent(studentNameParam);
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("BrailleSense");
            int sessionId = com.studentgui.apphelpers.Database.createProgressSession(studentId, ptId, dateParam);
            String[] codes = inputs.keySet().toArray(String[]::new);
            int[] scores = new int[codes.length];
            for (int i = 0; i < codes.length; i++) {
                scores[i] = inputs.get(codes[i]).getValue();
            }
            com.studentgui.apphelpers.Database.insertAssessmentResults(sessionId, ptId, codes, scores);
            LOG.info("BrailleSense data saved for {}", studentNameParam);
            com.studentgui.apphelpers.UiNotifier.show("BrailleSense data saved.");
            com.studentgui.apphelpers.dto.AssessmentPayload payload = new com.studentgui.apphelpers.dto.AssessmentPayload(sessionId, codes, scores);
            java.nio.file.Path jsonOut = com.studentgui.apphelpers.SessionJsonWriter.writeSessionJson(this.studentNameParam, "BrailleSense", payload, sessionId);
            if (jsonOut == null) {
                LOG.warn("Unable to save BrailleSense session JSON for sessionId={}", sessionId);
            }
            try {
                java.nio.file.Path out = com.studentgui.apphelpers.Helpers.APP_HOME
                        .resolve("StudentDataFiles").resolve(com.studentgui.apphelpers.Helpers.safeName(this.studentNameParam)).resolve("plots");
                java.nio.file.Files.createDirectories(out);
                java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_DATE;
                String dateStr = this.dateParam != null ? this.dateParam.format(df) : java.time.LocalDate.now().toString();
                String baseName = "BrailleSense-" + sessionId + "-" + dateStr;

                com.studentgui.apphelpers.Database.ResultsWithDates rwd = com.studentgui.apphelpers.Database.fetchLatestAssessmentResultsWithDates(this.studentNameParam, "BrailleSense", Integer.MAX_VALUE);
                java.util.Map<String, java.nio.file.Path> groups = null;
                String[] labels = java.util.Arrays.stream(this.parts).map(x -> x[1]).toArray(String[]::new);
                if (rwd != null && rwd.rows != null && !rwd.rows.isEmpty()) {
                    graph.updateWithGroupedDataByDate(rwd.dates, rwd.rows, codes, labels);
                    groups = graph.saveGroupedCharts(out, baseName, 1000, 240);
                    java.time.LocalDate headerDate = rwd.dates.get(rwd.dates.size() - 1);
                    dateStr = headerDate.format(df);
                } else {
                    java.util.List<java.util.List<Integer>> rowsList = new java.util.ArrayList<>();
                    java.util.List<Integer> latest = new java.util.ArrayList<>();
                    for (int i = 0; i < codes.length; i++) {
                        latest.add(inputs.get(codes[i]).getValue());
                    }
                    rowsList.add(latest);
                    graph.updateWithGroupedData(rowsList, codes);
                    groups = graph.saveGroupedCharts(out, baseName, 1000, 240);
                }

                if (groups == null) {
                    groups = new java.util.LinkedHashMap<>();
                }
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
                    java.nio.file.Path htmlFile = out.resolve(baseName + ".html");
                    java.nio.file.Files.writeString(htmlFile, html.toString(), java.nio.charset.StandardCharsets.UTF_8);
                    LOG.info("Wrote BrailleSense HTML session report {}", htmlFile);
                } catch (java.io.IOException ioex) {
                    LOG.warn("Unable to write BrailleSense HTML report: {}", ioex.toString());
                }
            } catch (java.io.IOException ioe) {
                LOG.warn("Unable to save BrailleSense per-phase plots or markdown report: {}", ioe.toString());
            }
        } catch (SQLException ex) {
            LOG.error("Error saving braillesense data", ex);
        }
    }

    // plotting is handled via submit/save which updates the shared graph and saves a static PNG
}
