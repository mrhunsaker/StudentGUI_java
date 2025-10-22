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
            try { java.awt.Desktop.getDesktop().open(plotPath.toFile()); }
            catch (java.io.IOException | UnsupportedOperationException | SecurityException ex) { com.studentgui.apphelpers.UiNotifier.show("Unable to open plot: " + plotPath.getFileName().toString()); }
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
                java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_DATE;
                java.nio.file.Path file = out.resolve("CVI-" + this.dateParam.format(df) + ".png");
                graph.saveChart(file, 800, 400);
                LOG.info("Saved CVI plot to {}", file);
            } catch (java.io.IOException ex) {
                LOG.warn("Unable to save CVI plot image: {}", ex.toString());
            }
        } catch (SQLException ex) {
            LOG.error("Error saving CVI data", ex);
            com.studentgui.apphelpers.UiNotifier.show("Database error saving CVI data: " + ex.getMessage());
        }
    }

    // Plotting is handled as part of save(): the submit action updates the shared
    // graph and writes a static PNG into the student's plots folder.
}
