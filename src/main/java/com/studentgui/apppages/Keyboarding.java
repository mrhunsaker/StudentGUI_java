package com.studentgui.apppages;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Touch-typing and keyboarding skills assessment page.
 *
 * <p>Unlike other assessment pages that use phase-score grids, this page captures
 * structured performance metrics for keyboarding practice sessions:</p>
 *
 * <ul>
 *   <li><b>Program:</b> Name of the typing curriculum or software (e.g., TypingClub, KeyBlaze, Braille2000)</li>
 *   <li><b>Topic:</b> Specific lesson, module, or exercise completed (e.g., "Home Row Mastery", "Lesson 12")</li>
 *   <li><b>Speed (WPM):</b> Words per minute achieved during the timed exercise</li>
 *   <li><b>Accuracy (%):</b> Percentage of characters typed correctly</li>
 * </ul>
 *
 * <p><b>Data Persistence:</b></p>
 * <ul>
 *   <li>Values persisted via {@link com.studentgui.apphelpers.Database#insertKeyboardingResult} to the {@code KeyboardingResult} table</li>
 *   <li>JSON export: {@code StudentDataFiles/<student>/Sessions/Keyboarding/Keyboarding-<sessionId>-<timestamp>.json}</li>
 *   <li>Metadata-only reports (no plots): Markdown and HTML files in {@code reports/} with session details</li>
 * </ul>
 *
 * <p><b>Validation and Error Handling:</b></p>
 * <ul>
 *   <li>Speed and Accuracy fields must contain whole numbers (non-negative integers)</li>
 *   <li>Empty speed/accuracy fields default to 0 for leniency</li>
 *   <li>Invalid input triggers error dialogs and field focus for correction</li>
 * </ul>
 *
 * <p>The shared {@link JLineGraph} component is present for UI consistency but is not populated
 * with keyboarding data (keyboarding does not use assessment parts). Implements
 * {@link com.studentgui.app.DateChangeListener} and {@link com.studentgui.app.StudentChangeListener}
 * for title updates when global selections change.</p>
 *
 * @see com.studentgui.apphelpers.Database#insertKeyboardingResult
 * @see com.studentgui.apphelpers.dto.KeyboardingPayload
 */
public class Keyboarding extends JPanel implements com.studentgui.app.DateChangeListener, com.studentgui.app.StudentChangeListener {
    private static final Logger LOG = LoggerFactory.getLogger(Keyboarding.class);
    /** Text field for the program or curriculum name. */
    private final JTextField programField, topicField, speedField, accuracyField;

    /** Shared graph component (present but not used for keyboarding plotting). */
    private final JLineGraph lineGraph;

    /** Selected student's display name for saves/refreshes (may be null). */
    private String studentNameParam;
    /** Page header label. */
    private JLabel titleLabel;
    /** Base title text for the Keyboarding page; date suffix appended in UI. */
    private final String baseTitle = "Keyboarding Skills";

    /** Session date associated with persisted keyboarding results. */
    private LocalDate dateParam;

    /**
     * Construct the Keyboarding page for a specific student and session date.
     *
     * @param studentName selected student's display name (may be null)
     * @param date session date used for persisted results
     * @param lineGraph shared graph component (unused for keyboarding results)
     */
    public Keyboarding(String studentName, LocalDate date, JLineGraph lineGraph) {
    this.studentNameParam = (studentName == null || studentName.trim().isEmpty()) ? com.studentgui.apphelpers.Helpers.defaultStudent() : studentName;
        this.dateParam = date;
        this.lineGraph = lineGraph;
        setLayout(new BorderLayout());

    JPanel p = new JPanel(new GridBagLayout());
    JPanel view = new JPanel(new BorderLayout());
    view.add(p, BorderLayout.NORTH);
    view.setBorder(javax.swing.BorderFactory.createEmptyBorder(20,20,20,20));
    JScrollPane scroll = new JScrollPane(view);
    scroll.getAccessibleContext().setAccessibleName("Keyboarding data entry scroll pane");
    p.setBorder(javax.swing.BorderFactory.createEmptyBorder(20,20,20,20));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets=new Insets(2,2,2,2); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.NORTHWEST;
    this.titleLabel = new JLabel(baseTitle, JLabel.LEFT);
    this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(Font.BOLD,28f));
    this.titleLabel.getAccessibleContext().setAccessibleName("Keyboarding Skills Title");
    gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2; p.add(this.titleLabel, gbc);

    gbc.gridwidth=1;
    // Normalize label width to the PhaseScoreField global width so inputs align
    int globalLabel = com.studentgui.uicomp.PhaseScoreField.getGlobalLabelWidth();
    gbc.gridy=1; gbc.gridx=0; JLabel programLabel = new JLabel("Program:"); programLabel.setPreferredSize(new Dimension(globalLabel, programLabel.getPreferredSize().height)); p.add(programLabel, gbc); gbc.gridx=1; programField = new JTextField(); programField.setPreferredSize(new Dimension(300,24)); programField.setToolTipText("Name of the program or curriculum"); programField.getAccessibleContext().setAccessibleName("Program"); p.add(programField, gbc); programLabel.setLabelFor(programField);
    gbc.gridy=2; gbc.gridx=0; JLabel topicLabel = new JLabel("Topic:"); topicLabel.setPreferredSize(new Dimension(globalLabel, topicLabel.getPreferredSize().height)); p.add(topicLabel, gbc); gbc.gridx=1; topicField = new JTextField(); topicField.setPreferredSize(new Dimension(300,24)); topicField.setToolTipText("Topic or lesson name"); topicField.getAccessibleContext().setAccessibleName("Topic"); p.add(topicField, gbc); topicLabel.setLabelFor(topicField);
    gbc.gridy=3; gbc.gridx=0; JLabel speedLabel = new JLabel("Speed (WPM):"); speedLabel.setPreferredSize(new Dimension(globalLabel, speedLabel.getPreferredSize().height)); p.add(speedLabel, gbc); gbc.gridx=1; speedField = new JTextField("0"); speedField.setPreferredSize(new Dimension(100,24)); speedField.setToolTipText("Words per minute"); speedField.getAccessibleContext().setAccessibleName("Speed (WPM)"); p.add(speedField, gbc); speedLabel.setLabelFor(speedField);
    gbc.gridy=4; gbc.gridx=0; JLabel accuracyLabel = new JLabel("Accuracy (%):"); accuracyLabel.setPreferredSize(new Dimension(globalLabel, accuracyLabel.getPreferredSize().height)); p.add(accuracyLabel, gbc); gbc.gridx=1; accuracyField = new JTextField("0"); accuracyField.setPreferredSize(new Dimension(100,24)); accuracyField.setToolTipText("Accuracy percentage"); accuracyField.getAccessibleContext().setAccessibleName("Accuracy (%)"); p.add(accuracyField, gbc); accuracyLabel.setLabelFor(accuracyField);

    gbc.gridy=5; gbc.gridx=0; gbc.gridwidth=GridBagConstraints.REMAINDER;
    JButton submit = new JButton("Submit Data");
    submit.setPreferredSize(new java.awt.Dimension(0, 32));
    submit.addActionListener((ActionEvent e)-> { submitData(); refreshGraph(); });
    submit.setToolTipText("Save keyboarding result for selected student");
    submit.setMnemonic(KeyEvent.VK_S);
    submit.getAccessibleContext().setAccessibleName("Submit Keyboarding Data");
    p.add(submit, gbc);
    gbc.gridwidth = 1;
    // Removed separate Refresh Graph button; Submit Data now triggers refreshGraph

    add(scroll, BorderLayout.CENTER);
    add(this.lineGraph, BorderLayout.SOUTH);

    SwingUtilities.invokeLater(()->{ p.setPreferredSize(p.getPreferredSize()); updateTitleDate(); revalidate(); });

        com.studentgui.apphelpers.Helpers.createFolderHierarchy();
        initDatabase();
        refreshGraph();
    }

    /**
     * Ensure the keyboarding progress type exists in the canonical schema.
     */
    private void initDatabase() {
        try {
            com.studentgui.apphelpers.Database.getOrCreateProgressType("Keyboarding");
        } catch (SQLException ex) {
            LOG.error("Error ensuring Keyboarding progress type", ex);
        }
    }

    /**
     * Validate keyboarding inputs (speed and accuracy as integers) and
     * persist a keyboarding result record for the selected student.
     */
    private void submitData() {
        if (this.studentNameParam == null || this.studentNameParam.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a student before saving keyboarding data.", "Missing student", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int studentId = com.studentgui.apphelpers.Database.getOrCreateStudent(this.studentNameParam);
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("Keyboarding");
            int sessionId = com.studentgui.apphelpers.Database.createProgressSession(studentId, ptId, this.dateParam);

            String program = programField.getText().trim();
            String topic = topicField.getText().trim();
            int speed;
            int accuracy;
            try {
                String sp = speedField.getText().trim(); speed = sp.isEmpty() ? 0 : Integer.parseInt(sp);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Please enter a whole number for Speed (WPM)", "Invalid input", JOptionPane.ERROR_MESSAGE);
                speedField.requestFocusInWindow();
                return;
            }
            try {
                String ac = accuracyField.getText().trim(); accuracy = ac.isEmpty() ? 0 : Integer.parseInt(ac);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Please enter a whole number for Accuracy (%)", "Invalid input", JOptionPane.ERROR_MESSAGE);
                accuracyField.requestFocusInWindow();
                return;
            }

            com.studentgui.apphelpers.Database.insertKeyboardingResult(sessionId, program, topic, speed, accuracy);
            LOG.info("Keyboarding data saved for {}", this.studentNameParam);
            com.studentgui.apphelpers.UiNotifier.show("Keyboarding data saved.");
            com.studentgui.apphelpers.dto.KeyboardingPayload payload = new com.studentgui.apphelpers.dto.KeyboardingPayload(sessionId, program, topic, speed, accuracy);
            java.nio.file.Path jsonOut = com.studentgui.apphelpers.SessionJsonWriter.writeSessionJson(this.studentNameParam, "Keyboarding", payload, sessionId);
            if (jsonOut == null) {
                LOG.warn("Unable to save Keyboarding session JSON for sessionId={}", sessionId);
            }
            try {
                java.nio.file.Path plotsOut = com.studentgui.apphelpers.Helpers.studentPlotsDir(this.studentNameParam);
                java.nio.file.Path reportsOut = com.studentgui.apphelpers.Helpers.studentReportsDir(this.studentNameParam);
                java.nio.file.Files.createDirectories(plotsOut);
                java.nio.file.Files.createDirectories(reportsOut);
                java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_DATE;
                String dateStr = this.dateParam != null ? this.dateParam.format(df) : java.time.LocalDate.now().toString();
                String baseName = "Keyboarding-" + sessionId + "-" + dateStr;

                // Keyboarding doesn't have grouped codes; produce a small HTML/MD with metadata
                StringBuilder md = new StringBuilder();
                md.append("# ").append(this.studentNameParam == null ? "Unknown Student" : this.studentNameParam).append(" - ").append(dateStr).append("\n\n");
                md.append("**Program:** ").append(program == null || program.isEmpty() ? "(none)" : program).append("  \n\n");
                md.append("**Topic:** ").append(topic == null || topic.isEmpty() ? "(none)" : topic).append("  \n\n");
                md.append("**Speed (WPM):** ").append(String.valueOf(speed)).append("  \n\n");
                md.append("**Accuracy (%):** ").append(String.valueOf(accuracy)).append("  \n\n");
                java.nio.file.Path mdFile = reportsOut.resolve(baseName + ".md");
                java.nio.file.Files.writeString(mdFile, md.toString(), java.nio.charset.StandardCharsets.UTF_8);

                try {
                    StringBuilder html = new StringBuilder();
                    html.append("<!doctype html><html><head><meta charset=\"utf-8\"><title>");
                    html.append(this.studentNameParam == null ? "Student Report" : this.studentNameParam).append(" - ").append(dateStr).append("</title>");
                    html.append("<style>body{font-family:sans-serif;margin:20px;} .meta{margin-bottom:12px;} .swatch{width:18px;height:12px;border:1px solid #333;display:inline-block;vertical-align:middle;margin-right:8px;}</style>");
                    html.append("</head><body>");
                    html.append("<h1>").append(this.studentNameParam == null ? "Unknown Student" : this.studentNameParam).append(" - ").append(dateStr).append("</h1>");
                    html.append("<div class=\"meta\">\n");
                    html.append("<p><strong>Program:</strong> ").append(program == null || program.isEmpty() ? "(none)" : program).append("</p>");
                    html.append("<p><strong>Topic:</strong> ").append(topic == null || topic.isEmpty() ? "(none)" : topic).append("</p>");
                    html.append("<p><strong>Speed (WPM):</strong> ").append(String.valueOf(speed)).append("</p>");
                    html.append("<p><strong>Accuracy (%):</strong> ").append(String.valueOf(accuracy)).append("</p>");
                    html.append("</div>");
                    html.append("</body></html>");
                    java.nio.file.Path htmlFile = reportsOut.resolve(baseName + ".html");
                    java.nio.file.Files.writeString(htmlFile, html.toString(), java.nio.charset.StandardCharsets.UTF_8);
                    LOG.info("Wrote Keyboarding session report {}", htmlFile);
                } catch (java.io.IOException ioex) {
                    LOG.warn("Unable to write Keyboarding HTML report: {}", ioex.toString());
                }
            } catch (java.io.IOException ioe) {
                LOG.warn("Unable to save Keyboarding report: {}", ioe.toString());
            }
        } catch (SQLException ex) {
            LOG.error("DB error saving keyboarding data", ex);
            JOptionPane.showMessageDialog(this, "Database error saving keyboarding data: " + ex.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Refresh the keyboarding visualization. Currently keyboarding results are
     * stored in a separate table and this method logs the request.
     */
    private void refreshGraph() {
        LOG.info("Keyboarding refresh requested for {}", studentNameParam);
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
}
