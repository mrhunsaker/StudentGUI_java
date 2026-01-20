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
 * Braille skills progression assessment page.
 *
 * <p>Provides a comprehensive user interface for tracking student proficiency across
 * 64 standardized Braille skills organized into 8 progressive phases following the
 * Mangold Developmental Program sequence:</p>
 *
 * <ul>
 *   <li><b>Phase 1 (P1_1–P1_4):</b> Foundational tracking and discrimination skills</li>
 *   <li><b>Phase 2 (P2_1–P2_15):</b> Mangold letter progression (G C L → V J)</li>
 *   <li><b>Phase 3 (P3_1–P3_15):</b> Contractions, wordsigns, and Grade 2 Braille basics</li>
 *   <li><b>Phase 4 (P4_1–P4_4):</b> Indicators (Grade 1, capitals, numeric mode, typeform)</li>
 *   <li><b>Phase 5 (P5_1–P5_4):</b> Document formatting (page numbers, headings, lists, poetry)</li>
 *   <li><b>Phase 6 (P6_1–P6_7):</b> Basic Nemeth Math Code (operations, shapes, fractions)</li>
 *   <li><b>Phase 7 (P7_1–P7_8):</b> Advanced Math (algebra, indices, radicals, functions, Greek)</li>
 *   <li><b>Phase 8 (P8_1–P8_7):</b> Higher mathematics (modifiers, calculus, probability)</li>
 * </ul>
 *
 * <p><b>Data Flow and Persistence:</b></p>
 * <ul>
 *   <li>Each skill is represented by a {@link com.studentgui.uicomp.PhaseScoreField} accepting integer scores (0–4 typical range)</li>
 *   <li>On submission, values are persisted to the normalized schema via {@link com.studentgui.apphelpers.Database#insertAssessmentResults}</li>
 *   <li>A timestamped JSON export is written to {@code StudentDataFiles/<student>/Sessions/Braille/}</li>
 *   <li>Time-series plots are generated per phase group and saved as PNG images to {@code plots/}</li>
 *   <li>Markdown and HTML reports are generated combining all phase plots with legend and metadata</li>
 * </ul>
 *
 * <p><b>Generated Artifacts:</b></p>
 * <ul>
 *   <li><b>JSON session file:</b> {@code Braille-<sessionId>-<timestamp>.json}</li>
 *   <li><b>Phase plots:</b> {@code Braille-<sessionId>-<date>-P<N>.png} (8 phase groups)</li>
 *   <li><b>Markdown report:</b> {@code reports/Braille-<sessionId>-<date>.md}</li>
 *   <li><b>HTML report:</b> {@code reports/Braille-<sessionId>-<date>.html} with embedded plots and color-coded legends</li>
 * </ul>
 *
 * <p>The shared {@link JLineGraph} component visualizes recent session trends for the selected
 * student, grouped by phase to prevent overcrowding. This page implements {@link com.studentgui.app.DateChangeListener}
 * and {@link com.studentgui.app.StudentChangeListener} to refresh data when the global student or date selection changes.</p>
 *
 * @see com.studentgui.apphelpers.Database
 * @see JLineGraph
 * @see com.studentgui.uicomp.PhaseScoreField
 */
public class Braille extends JPanel implements com.studentgui.app.DateChangeListener, com.studentgui.app.StudentChangeListener {
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
    private String studentNameParam;
    /** Session date used when creating progress sessions. */
    private LocalDate dateParam;
    /** Title label component displayed in the page header. */
    private JLabel titleLabel;
    /** Base title text for the Braille page; a date suffix may be appended for display. */
    private final String baseTitle = "Braille Skills Progression";

    /**
     * Construct the Braille skills page for a given student and date.
     *
     * @param studentName the selected student name (may be null before selection)
     * @param date the session date to use when creating a progress session
     * @param lineGraph shared graph component used to display recent results
     */
    public Braille(final String studentName, final LocalDate date, final JLineGraph lineGraph) {
        this.lineGraph = lineGraph; // Use the passed in graph instance
    this.studentNameParam = (studentName == null || studentName.trim().isEmpty()) ? com.studentgui.apphelpers.Helpers.defaultStudent() : studentName;
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
        for (int i = 0; i < this.parts.length; i++) {
            this.partCodes[i] = this.parts[i][0];
        }

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

    this.titleLabel = new JLabel(baseTitle);
    this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(Font.BOLD, 28f));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        dataEntryPanel.add(this.titleLabel, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.ipady = 20;
        dataEntryPanel.add(new JPanel(), gbc);

    // compute longest label width to align inputs
        String[] labels = java.util.Arrays.stream(parts).map(x->x[1]).toArray(String[]::new);
            int maxPx = com.studentgui.uicomp.PhaseScoreField.computeMaxLabelPixelWidth(com.studentgui.uicomp.PhaseScoreField.getLabelFont(), labels);
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
        if (p == null) {
            com.studentgui.apphelpers.UiNotifier.show("No Braille plot found for student");
        } else {
            try {
                java.awt.Desktop.getDesktop().open(p.toFile());
            } catch (java.io.IOException | UnsupportedOperationException | SecurityException ex) {
                com.studentgui.apphelpers.UiNotifier.show("Unable to open plot: " + p.getFileName().toString());
            }
        }
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
            updateTitleDate();
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
    *
    * Implementation note: arrays used to call {@code insertAssessmentResults}
    * are allocated dynamically based on the actual number of parts
    * ({@code partCodes.length}) so that the stored columns exactly match the
    * plotted series. This fixes a previous issue where fixed-size arrays
    * could become out-of-sync with the parts list.
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

            // Allocate arrays based on the actual number of parts so that
            // the submitted data and plotted series stay in sync.
            String[] codes = new String[this.partCodes.length];
            int[] scores = new int[this.partCodes.length];
            for (int i = 0; i < this.partCodes.length; i++) {
                codes[i] = this.partCodes[i];
                scores[i] = skillFields[i].getValue();
            }
            com.studentgui.apphelpers.Database.insertAssessmentResults(sessionId, ptId, codes, scores);
            LOG.info("Data submitted successfully via normalized schema.");
            com.studentgui.apphelpers.UiNotifier.show("Braille data saved.");
            com.studentgui.apphelpers.dto.AssessmentPayload payload = new com.studentgui.apphelpers.dto.AssessmentPayload(sessionId, codes, scores);
            java.nio.file.Path jsonOut = com.studentgui.apphelpers.SessionJsonWriter.writeSessionJson(this.studentNameParam, "Braille", payload, sessionId);
            if (jsonOut == null) {
                LOG.warn("Unable to save Braille session JSON for sessionId={}", sessionId);
            }
            try {
                java.nio.file.Path plotsOut = com.studentgui.apphelpers.Helpers.studentPlotsDir(this.studentNameParam);
                java.nio.file.Path reportsOut = com.studentgui.apphelpers.Helpers.studentReportsDir(this.studentNameParam);
                java.nio.file.Files.createDirectories(plotsOut);
                java.nio.file.Files.createDirectories(reportsOut);
                java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_DATE;
                String dateStr = this.dateParam != null ? this.dateParam.format(df) : java.time.LocalDate.now().toString();
                String baseName = "Braille-" + sessionId + "-" + dateStr;

                com.studentgui.apphelpers.Database.ResultsWithDates rwd = com.studentgui.apphelpers.Database.fetchLatestAssessmentResultsWithDates(this.studentNameParam, "Braille", Integer.MAX_VALUE);
                java.util.Map<String, java.nio.file.Path> groups = null;
                String[] labels = new String[this.parts.length];
                for (int i = 0; i < this.parts.length; i++) {
                    labels[i] = this.parts[i][1];
                }
                if (rwd != null && rwd.rows != null && !rwd.rows.isEmpty()) {
                    lineGraph.updateWithGroupedDataByDate(rwd.dates, rwd.rows, this.partCodes, labels);
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
                    lineGraph.updateWithGroupedData(rowsList, this.partCodes);
                    groups = lineGraph.saveGroupedCharts(plotsOut, baseName, 1000, 240);
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
                java.nio.file.Path mdFile = reportsOut.resolve(baseName + ".md");
                // images live in ../plots relative to reports
                String mdText = md.toString().replace("![](./", "![](../plots/");
                java.nio.file.Files.writeString(mdFile, mdText, java.nio.charset.StandardCharsets.UTF_8);

                // HTML report using shared palette
                try {
                    String[] palette = JLineGraph.PALETTE_HEX;
                    java.util.LinkedHashMap<String, java.util.List<Integer>> groupsIdx = new java.util.LinkedHashMap<>();
                    for (int i = 0; i < this.partCodes.length; i++) {
                        String code = this.partCodes[i];
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
                            String code = this.partCodes[idx];
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
                    String htmlStr = html.toString().replace("src=\"./", "src=\"../plots/");
                    java.nio.file.Files.writeString(htmlFile, htmlStr, java.nio.charset.StandardCharsets.UTF_8);
                    LOG.info("Wrote Braille HTML session report {}", htmlFile);
                } catch (java.io.IOException ioex) {
                    LOG.warn("Unable to write Braille HTML report: {}", ioex.toString());
                }

                LOG.info("Wrote Braille session report {} with {} group images", mdFile, groups.size());
            } catch (java.io.IOException | SQLException ex) {
                LOG.warn("Unable to save Braille per-phase plots or markdown report: {}", ex.toString());
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
                // Write to the consolidated per-run data dumps file when enabled
                if (Boolean.parseBoolean(com.studentgui.apphelpers.Settings.get("dump.enabled", "false"))) {
                    try {
                        String appHome = System.getProperty("APP_HOME", com.studentgui.apphelpers.Helpers.APP_HOME.toString());
                        String ts = System.getProperty("LOG_TS", String.valueOf(java.time.Instant.now().getEpochSecond()));
                        java.nio.file.Path logDir = java.nio.file.Paths.get(appHome).resolve("logs");
                        java.nio.file.Files.createDirectories(logDir);
                        java.nio.file.Path logFile = logDir.resolve("data_dumps_" + ts + ".log");
                        StringBuilder sb = new StringBuilder();
                        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ISO_DATE_TIME;
                        sb.append("[Braille]").append(System.lineSeparator());
                        sb.append(java.time.LocalDateTime.now().format(dtf)).append(" - student=").append(this.studentNameParam).append(System.lineSeparator());
                        sb.append("data=").append(allSkillValues.toString()).append(System.lineSeparator());
                        sb.append(System.lineSeparator());
                        java.nio.file.Files.writeString(logFile, sb.toString(), java.nio.charset.StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
                    } catch (java.io.IOException ioe) {
                        LOG.trace("Unable to write braille load log: {}", ioe.toString());
                    }
                }
            } else {
                LOG.info("No data to plot; showing grouped placeholders.");
                lineGraph.showEmptyGrouped(this.partCodes);
            }
        } catch (SQLException e) {
            LOG.error("SQL error refreshing braille graph", e);
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
        this.studentNameParam = newStudent != null ? newStudent : "Unknown Student";
        SwingUtilities.invokeLater(() -> {
            refreshGraph();
            updateTitleDate();
        });
    }
    
    /**
     * Update the page title label to include the current session date.
     * Falls back to base title if date formatting fails.
     */
    private void updateTitleDate() {
        try {
            String dateStr = this.dateParam != null ? this.dateParam.toString() : java.time.LocalDate.now().toString();
            this.titleLabel.setText(baseTitle + " - " + dateStr);
        } catch (Exception ex) {
            this.titleLabel.setText(baseTitle);
        }
    }
    

}
