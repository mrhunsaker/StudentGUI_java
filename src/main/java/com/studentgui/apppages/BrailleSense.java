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
    private final Map<String,PhaseScoreField> inputs = new LinkedHashMap<>();
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
    view.setBorder(javax.swing.BorderFactory.createEmptyBorder(20,20,20,20));
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
    gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = GridBagConstraints.REMAINDER;
    dataEntryPanel.add(titleLabel, gbc);

    gbc.gridy = 1; gbc.gridwidth = GridBagConstraints.REMAINDER; gbc.ipady = 20;
    dataEntryPanel.add(new JPanel(), gbc);

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

    // compute pixel width using font metrics so labels align precisely
    String[] labels = java.util.Arrays.stream(this.parts).map(x->x[1]).toArray(String[]::new);
        int maxPx = com.studentgui.uicomp.PhaseScoreField.computeMaxLabelPixelWidth(titleLabel.getFont(), labels);
            com.studentgui.uicomp.PhaseScoreField.setGlobalLabelWidth(Math.min(360, Math.max(200, maxPx + 50)));
    int row = 1;
        for (String[] def: this.parts) {
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
            PhaseScoreField tf = new PhaseScoreField(def[1], 0);
            tf.setName("braillesense_" + def[0]);
            tf.getAccessibleContext().setAccessibleName(def[1]);
            tf.setToolTipText("Enter score for " + def[1]);
            dataEntryPanel.add(tf, gbc);
            inputs.put(def[0], tf);
            row++;
        }

    // Place Submit and Open Latest side-by-side to match IOS/ScreenReader styling
    gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
    JButton submit = new JButton("Submit Data");
    submit.setPreferredSize(new java.awt.Dimension(0, 32));
    submit.addActionListener((ActionEvent e) -> save());
    submit.setMnemonic(KeyEvent.VK_S);
    submit.setToolTipText("Save BrailleSense scores (Alt+S)");
    submit.getAccessibleContext().setAccessibleName("Submit BrailleSense Data");
    submit.setName("braillesense_submit");
    dataEntryPanel.add(submit, gbc);

    gbc.gridx = 1; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
    JButton openLatest = new JButton("Open Latest Plot");
    openLatest.setPreferredSize(new java.awt.Dimension(0, 32));
    openLatest.addActionListener((ActionEvent e) -> {
        java.nio.file.Path p = com.studentgui.apphelpers.Helpers.latestPlotPath(this.studentNameParam, "BrailleSense");
        if (p == null) com.studentgui.apphelpers.UiNotifier.show("No BrailleSense plot found for student");
        else {
            try { java.awt.Desktop.getDesktop().open(p.toFile()); }
            catch (java.io.IOException | UnsupportedOperationException | SecurityException ex) { com.studentgui.apphelpers.UiNotifier.show("Unable to open plot: " + p.getFileName().toString()); }
        }
    });
    dataEntryPanel.add(openLatest, gbc);

    // Filler to consume remaining horizontal space
    gbc.gridx = 2; gbc.gridwidth = GridBagConstraints.REMAINDER; gbc.anchor = GridBagConstraints.WEST;
    dataEntryPanel.add(new JPanel(), gbc);

    dataEntryScrollPane.getAccessibleContext().setAccessibleName("BrailleSense data entry scroll pane");
    add(dataEntryScrollPane, BorderLayout.CENTER); add(graph, BorderLayout.SOUTH);
        SwingUtilities.invokeLater(()->{ dataEntryPanel.setPreferredSize(dataEntryPanel.getPreferredSize()); revalidate(); });
    SwingUtilities.invokeLater(() -> {
        for (var e: inputs.values()) LOG.debug("BrailleSense field {} labelWidth={} spinnerX={} gap={}", e.getLabel(), e.getLabelWrapWidth(), e.getSpinnerX(), e.getActualGap());
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
            if (jsonOut == null) LOG.warn("Unable to save BrailleSense session JSON for sessionId={}", sessionId);
            try {
                java.nio.file.Path out = com.studentgui.apphelpers.Helpers.APP_HOME.resolve("StudentDataFiles").resolve(com.studentgui.apphelpers.Helpers.safeName(this.studentNameParam)).resolve("plots");
                java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_DATE;
                java.nio.file.Path file = out.resolve("BrailleSense-" + this.dateParam.format(df) + ".png");
                graph.saveChart(file, 800, 400);
                LOG.info("Saved BrailleSense plot to {}", file);
            } catch (java.io.IOException ex) {
                LOG.warn("Unable to save BrailleSense plot image: {}", ex.toString());
            }
        } catch (SQLException ex) { LOG.error("Error saving braillesense data", ex); }
    }

    // plotting is handled via submit/save which updates the shared graph and saves a static PNG
}
