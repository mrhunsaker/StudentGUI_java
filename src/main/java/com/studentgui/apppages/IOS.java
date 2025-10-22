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
 * iOS / iPadOS skills progression page.
 * <p>
 * Presents a map of device and app related skills keyed by part codes and
 * allows saving and plotting of recent assessment sessions using the shared
 * {@link JLineGraph} instance.
 * </p>
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
        this.studentNameParam = studentName;
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
    title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
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

    java.awt.Font labelFont = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 12);
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
    // Buttons: Save iOS Data + Open Latest Plot (side-by-side, match IOS styling)
    gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST;
    javax.swing.JPanel buttonRow = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
    buttonRow.setOpaque(false);

    JButton save = new JButton("Save iOS Data");
    save.setPreferredSize(new java.awt.Dimension(0, 32));
    save.addActionListener((ActionEvent e) -> { save(); plot(); });
    save.setToolTipText("Save iOS assessment for selected student");
    save.setMnemonic(KeyEvent.VK_S);
    save.getAccessibleContext().setAccessibleName("Save iOS Data");
    buttonRow.add(save);

    JButton openLatest = new JButton("Open Latest Plot");
    openLatest.setPreferredSize(new java.awt.Dimension(0, 32));
    openLatest.addActionListener((ActionEvent e) -> {
        java.nio.file.Path pth = com.studentgui.apphelpers.Helpers.latestPlotPath(this.studentNameParam, "iOS");
        if (pth == null) com.studentgui.apphelpers.UiNotifier.show("No iOS plot found for student");
        else { try { java.awt.Desktop.getDesktop().open(pth.toFile()); } catch (java.io.IOException | UnsupportedOperationException | SecurityException ex) { com.studentgui.apphelpers.UiNotifier.show("Unable to open plot: " + pth.getFileName().toString()); } }
    });
    buttonRow.add(openLatest);

    p.add(buttonRow, gbc);
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
            int idx = 0; for (String k: keys) codes[idx++] = k;
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
                java.nio.file.Path out = com.studentgui.apphelpers.Helpers.APP_HOME.resolve("StudentDataFiles").resolve(com.studentgui.apphelpers.Helpers.safeName(this.studentNameParam)).resolve("plots");
                java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_DATE;
                java.nio.file.Path file = out.resolve("iOS-" + this.dateParam.format(df) + ".png");
                graph.saveChart(file, 800, 400);
                LOG.info("Saved iOS plot to {}", file);
            } catch (java.io.IOException ex) {
                LOG.warn("Unable to save iOS plot image: {}", ex.toString());
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
                graph.updateWithData(data);
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
                        try { java.awt.Desktop.getDesktop().open(file.toFile()); } catch (java.io.IOException | UnsupportedOperationException | SecurityException ex) { LOG.debug("Could not open iOS plot file: {}", ex.toString()); }
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
