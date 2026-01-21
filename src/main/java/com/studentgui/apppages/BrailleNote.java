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
 * HumanWare BrailleNote Touch Plus (BNT+) proficiency assessment page.
 *
 * <p>Evaluates student competency with the BrailleNote Touch Plus refreshable braille notetaker
 * and productivity device across 52 skills organized into 12 functional domains:</p>
 *
 * <ul>
 *   <li><b>Phase 1 (P1_1–P1_9): Device Fundamentals and Core Applications</b>
 *     <ul>
 *       <li>Physical layout (braille keyboard, navigation keys, touchscreen, ports)</li>
 *       <li>Setup procedures and universal commands (power, mode switching, context menus)</li>
 *       <li>BNT+ navigation paradigm (gestures, quick keys, braille commands)</li>
 *       <li>File management (folders, copy/paste, rename, delete)</li>
 *       <li>Word processor (KeyWord): document creation, editing, formatting</li>
 *       <li>Email (KeyMail): compose, send, receive, attachments</li>
 *       <li>Internet browsing (KeyWeb): navigation, bookmarks, forms</li>
 *       <li>Calculator and KeyMath (arithmetic, scientific functions)</li>
 *     </ul>
 *   </li>
 *   <li><b>Phase 2 (P2_1–P2_7): Productivity Suite Applications</b>
 *     <ul>
 *       <li>Calendar management (appointments, reminders, recurring events)</li>
 *       <li>KeyBRF (Braille file viewer/editor)</li>
 *       <li>KeyFiles (file explorer and organizer)</li>
 *       <li>KeyMail (advanced email features)</li>
 *       <li>KeyWeb (advanced browsing, accessibility modes)</li>
 *       <li>KeyCalc (spreadsheet concepts)</li>
 *       <li>KeyWord (advanced formatting, styles, tables)</li>
 *     </ul>
 *   </li>
 *   <li><b>Phase 3 (P3_1–P3_7): Advanced Applications and Accessibility</b>
 *     <ul>
 *       <li>KeySlides (presentation creation and delivery)</li>
 *       <li>KeyCode (text editor with syntax highlighting for programming)</li>
 *       <li>Third-party app integration (Dropbox, Google Drive, OneDrive)</li>
 *       <li>Braille input configuration (computer braille, contracted, literary)</li>
 *       <li>Braille output settings (display mode, translation tables)</li>
 *       <li>Device settings and preferences</li>
 *       <li>Accessibility features (speech output, magnification, contrast)</li>
 *     </ul>
 *   </li>
 *   <li><b>Phase 4 (P4_1–P4_3): Advanced File and Cloud Management</b></li>
 *   <li><b>Phase 5 (P5_1–P5_4): Collaboration and Export Workflows</b></li>
 *   <li><b>Phase 6 (P6_1–P6_3): App Ecosystem and Troubleshooting</b></li>
 *   <li><b>Phase 7 (P7_1–P7_4): Automation and Customization</b></li>
 *   <li><b>Phase 8 (P8_1–P8_5): Peripheral Integration</b> (Bluetooth/USB devices, displays, audio/video)</li>
 *   <li><b>Phase 9 (P9_1–P9_4): Security and Network Configuration</b></li>
 *   <li><b>Phase 10 (P10_1–P10_3): Speech Engine Customization</b></li>
 *   <li><b>Phase 11 (P11_1–P11_5): Maintenance and Support</b> (firmware, diagnostics, warranty)</li>
 *   <li><b>Phase 12 (P12_1–P12_4): Community and Online Resources</b></li>
 * </ul>
 *
 * <p><b>Data Management and Artifacts:</b></p>
 * <ul>
 *   <li>Scores captured via {@link com.studentgui.uicomp.PhaseScoreField} (integer 0–4 typical)</li>
 *   <li>Persisted to normalized schema via {@link com.studentgui.apphelpers.Database#insertAssessmentResults}</li>
 *   <li>JSON export: {@code StudentDataFiles/<student>/Sessions/BrailleNote/BrailleNote-<sessionId>-<timestamp>.json}</li>
 *   <li>Phase-grouped time-series plots: {@code plots/BrailleNote-<sessionId>-<date>-P<N>.png} (12 phase groups)</li>
 *   <li>Markdown and HTML reports with embedded plots and color-coded legends</li>
 * </ul>
 *
 * <p>The shared {@link JLineGraph} visualizes recent session trends grouped by phase prefix.
 * Implements {@link com.studentgui.app.DateChangeListener} and {@link com.studentgui.app.StudentChangeListener}
 * for dynamic updates when global student/date selections change.</p>
 *
 * @see com.studentgui.apphelpers.Database
 * @see JLineGraph
 * @see com.studentgui.uicomp.PhaseScoreField
 */
public class BrailleNote extends JPanel implements com.studentgui.app.DateChangeListener, com.studentgui.app.StudentChangeListener {
    private static final Logger LOG = LoggerFactory.getLogger(BrailleNote.class);

    /** Inputs for each BrailleNote skill. */
    private final com.studentgui.uicomp.PhaseScoreField[] skillFields;
    /** Canonical assessment part codes and labels for BrailleNote. */
    private final String[][] parts;
    /** Shared graph component for plotting results. */
    private final JLineGraph lineGraph; // Reference to the JLineGraph instance
    /** Display name of the selected student (may be null). */
    private String studentNameParam;
    /** Header title label for this page. */
    private JLabel titleLabel;
    /** Base page title string used when rendering the header (date appended). */
    private final String baseTitle = "BrailleNote Skills Progression";
    /** Session date associated with persisted progress. */
    private LocalDate dateParam;

    /**
     * Create the BrailleNote page for a specific student and date.
     *
     * @param studentName the selected student name (may be null until a student is chosen)
     * @param date the date for the session (used when creating a progress session)
     * @param lineGraph shared graph component used to display recent results
     */
    public BrailleNote(String studentName, LocalDate date, JLineGraph lineGraph) {
    this.studentNameParam = (studentName == null || studentName.trim().isEmpty()) ? com.studentgui.apphelpers.Helpers.defaultStudent() : studentName;
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

    // layout spacing handled by PhaseScoreField

    // compute pixel width using font metrics so labels align precisely
    String[] labelsArr = java.util.Arrays.stream(parts).map(x->x[1]).toArray(String[]::new);
    int maxPx = com.studentgui.uicomp.PhaseScoreField.computeMaxLabelPixelWidth(com.studentgui.uicomp.PhaseScoreField.getLabelFont(), labelsArr);
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
        if (p == null) {
            com.studentgui.apphelpers.UiNotifier.show("No BrailleNote plot found for student");
        } else {
            try {
                java.awt.Desktop.getDesktop().open(p.toFile());
            } catch (java.io.IOException | UnsupportedOperationException | SecurityException ex) {
                com.studentgui.apphelpers.UiNotifier.show("Unable to open plot: " + p.getFileName().toString());
            }
        }
    });
    dataEntryPanel.add(openLatestBtn, gbc);

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
     * Ensure the progress-type and assessment part rows for BrailleNote exist
     * in the normalized schema. This is safe to call repeatedly.
     */
    private void initDatabase() {
        try {
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("BrailleNote");
            String[] codes = new String[this.parts.length];
            for (int i = 0; i < this.parts.length; i++) {
                codes[i] = this.parts[i][0];
            }
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
            if (jsonOut == null) {
                LOG.warn("Unable to save BrailleNote session JSON for sessionId={}", sessionId);
            }
            try {
                java.nio.file.Path plotsOut = com.studentgui.apphelpers.Helpers.studentPlotsDir(this.studentNameParam);
                java.nio.file.Path reportsOut = com.studentgui.apphelpers.Helpers.studentReportsDir(this.studentNameParam);
                java.nio.file.Files.createDirectories(plotsOut);
                java.nio.file.Files.createDirectories(reportsOut);
                java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_DATE;
                String dateStr = this.dateParam != null ? this.dateParam.format(df) : java.time.LocalDate.now().toString();
                String baseName = "BrailleNote-" + sessionId + "-" + dateStr;

                com.studentgui.apphelpers.Database.ResultsWithDates rwd = com.studentgui.apphelpers.Database.fetchLatestAssessmentResultsWithDates(this.studentNameParam, "BrailleNote", Integer.MAX_VALUE);
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
                    md.append("![](./").append(e.getValue().getFileName().toString()).append(")\n\n");
                }
                java.nio.file.Path mdFile = reportsOut.resolve(baseName + ".md");
                String mdText = md.toString().replace("![](./", "![](../plots/");
                java.nio.file.Files.writeString(mdFile, mdText, java.nio.charset.StandardCharsets.UTF_8);

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
                    java.nio.file.Path htmlFile = reportsOut.resolve(baseName + ".html");
                    String htmlStr = html.toString().replace("src=\"./", "src=\"../plots/");
                    java.nio.file.Files.writeString(htmlFile, htmlStr, java.nio.charset.StandardCharsets.UTF_8);
                    LOG.info("Wrote BrailleNote HTML session report {}", htmlFile);
                } catch (java.io.IOException ioex) {
                    LOG.warn("Unable to write BrailleNote HTML report: {}", ioex.toString());
                }
            } catch (java.io.IOException ioe) {
                LOG.warn("Unable to save BrailleNote per-phase plots or markdown report: {}", ioe.toString());
            }
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
                String[] codes = new String[this.parts.length];
                for (int i = 0; i < this.parts.length; i++) {
                    codes[i] = this.parts[i][0];
                }
                lineGraph.updateWithGroupedData(allSkillValues, codes);
                    // Write to the consolidated per-run data dumps file when enabled
                    if (Boolean.parseBoolean(com.studentgui.apphelpers.Settings.get("dump.enabled", "false"))) {
                        try {
                            String appHome = System.getProperty("APP_HOME", com.studentgui.apphelpers.Helpers.APP_HOME.toString());
                            String ts = System.getProperty("LOG_TS", String.valueOf(java.time.Instant.now().getEpochSecond()));
                            java.nio.file.Path logDir = java.nio.file.Paths.get(appHome).resolve("logs");
                            java.nio.file.Files.createDirectories(logDir);
                            java.nio.file.Path logFile = logDir.resolve("data_dumps_" + ts + ".log");
                            StringBuilder sb = new StringBuilder();
                            sb.append("[BrailleNote]").append(System.lineSeparator());
                            sb.append(java.time.Instant.now().toString()).append(" - student=").append(this.studentNameParam).append(System.lineSeparator());
                            sb.append("data=").append(allSkillValues.toString()).append(System.lineSeparator());
                            sb.append(System.lineSeparator());
                            java.nio.file.Files.writeString(logFile, sb.toString(), java.nio.charset.StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
                        } catch (java.io.IOException ioe) {
                            LOG.trace("Unable to write BrailleNote load log: {}", ioe.toString());
                        }
                    }
            } else {
                LOG.info("No data to plot.");
                // Ensure the graph shows grouped placeholders matching the
                // canonical assessment part ordering so the UI displays
                // one subchart per P# prefix even with no sessions.
                String[] codes = new String[this.parts.length];
                for (int i = 0; i < this.parts.length; i++) {
                    codes[i] = this.parts[i][0];
                }
                lineGraph.showEmptyGrouped(codes);
            }
        } catch (SQLException e) {
            LOG.error("SQL error refreshing braille note graph", e);
        }
    }

    @Override
    /**
     * Update the currently displayed date for this page and refresh the UI.
     *
     * Sets the internal `dateParam` and schedules a refresh of the graph and
     * title on the Swing Event Dispatch Thread.
     *
     * @param newDate the date to display (may be null to use the current date)
     */

    public void dateChanged(LocalDate newDate) {
        this.dateParam = newDate;
        SwingUtilities.invokeLater(() -> {
            refreshGraph();
            updateTitleDate();
        });
    }

    @Override
    /**
     * Update the selected student for this page and refresh the UI.
     *
     * Sets the internal `studentNameParam` and schedules a refresh of the
     * graph and title on the Swing Event Dispatch Thread.
     *
     * @param newStudent student identifier (name or id) to display; may be null
     */

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
    

}
