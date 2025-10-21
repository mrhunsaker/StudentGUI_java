package com.studentgui.uicomp;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

/**
 * Reusable component that renders a wrapped descriptive label and a compact
 * integer input (0..4). The label is a non-editable JTextArea that wraps at
 * ~200px; the component adds a 20px left inset so the label appears offset.
 * The spinner is aligned to the first line of the label.
 */
public class PhaseScoreField extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JTextArea labelArea;
    private final JSpinner spinner;
    private final JPanel labelWrap;
    // Global label width (pixels) used to make all rows align; default ~200
    private static int GLOBAL_LABEL_WIDTH_PX = 200;

    private final JPanel spacer;

    public PhaseScoreField(String labelText, int initial) {
        super(new GridBagLayout());
    this.labelArea = new JTextArea(labelText);
    labelArea.setLineWrap(true);
    labelArea.setWrapStyleWord(true);
    labelArea.setEditable(false);
    labelArea.setOpaque(false);
    labelArea.setFocusable(false);
    // Use explicit font so the appearance doesn't change when switching LAFs
    Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    labelArea.setFont(labelFont);
    // Constrain width to the configured global label width so it doesn't expand.
    // Pages set GLOBAL_LABEL_WIDTH_PX to (maxLabelPx + 50). We render the label
    // area at (GLOBAL - 50) and insert a 50px spacer so the spinner sits
    // exactly 50px after the longest label text.
    int prefHeight = computePreferredHeight(labelFont, 2);
    int labelWidth = Math.max(40, GLOBAL_LABEL_WIDTH_PX - 50);
    java.awt.Dimension fixed = new java.awt.Dimension(labelWidth, prefHeight);
    // Wrap the JTextArea in a small container to guarantee horizontal size
    this.labelWrap = new JPanel(new java.awt.BorderLayout());
    this.labelWrap.setPreferredSize(fixed);
    this.labelWrap.setMinimumSize(fixed);
    this.labelWrap.setMaximumSize(new java.awt.Dimension(labelWidth, Short.MAX_VALUE));
    this.labelWrap.add(labelArea, java.awt.BorderLayout.CENTER);

        this.spinner = new JSpinner(new SpinnerNumberModel(initial, 0, 4, 1));
        JComponent editor = spinner.getEditor();
        // Set explicit font for spinner editor to keep sizing consistent across themes
        Font spinnerFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        editor.setFont(spinnerFont);
        // The editor is typically a JSpinner.DefaultEditor containing a JTextField
        try {
            java.lang.reflect.Field f = editor.getClass().getDeclaredField("textField");
            f.setAccessible(true);
            Object tf = f.get(editor);
            if (tf instanceof javax.swing.JTextField) ((javax.swing.JTextField) tf).setFont(spinnerFont);
        } catch (ReflectiveOperationException ignored) { }
        editor.setPreferredSize(new Dimension(48, 20));
        spinner.setPreferredSize(new Dimension(48, 24));

        setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0)); // left inset 20px

    GridBagConstraints gbc = new GridBagConstraints();
    // Label: fixed preferred width, do not expand horizontally
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.NONE; // keep label at preferred size
    gbc.weightx = 0.0;
    gbc.insets = new Insets(2, 2, 2, 8);
    add(labelWrap, gbc);

    // Spacer: compute width so the spinner ends up 50px after the rendered label text
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0;
    // Compute rendered text pixel width for this label (safe to call here)
    int textPx = computeMaxLabelPixelWidth(labelFont, new String[] { labelText });
    int paddingWithinWrap = Math.max(0, labelWidth - textPx);
    int spacerWidth = Math.max(0, 50 - paddingWithinWrap);
    this.spacer = new JPanel(); this.spacer.setPreferredSize(new java.awt.Dimension(spacerWidth, 1));
    add(this.spacer, gbc);

    // Spinner sits immediately to the right of the spacer
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0;
    add(spinner, gbc);

    // Filler: consumes remaining horizontal space so the spinner doesn't get pushed to the far right
    gbc.gridx = 3;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    add(new JPanel(), gbc);

    // After layout, adjust spacer so the visible gap between label and spinner is exactly 50px
    javax.swing.SwingUtilities.invokeLater(() -> {
        int labelRight = labelWrap.getX() + labelWrap.getWidth();
        int actualGap = spinner.getX() - labelRight;
        int desiredGap = 50;
        int currentSpacer = this.spacer.getPreferredSize().width;
        int delta = desiredGap - actualGap;
        if (delta != 0) {
            int newWidth = Math.max(0, currentSpacer + delta);
            this.spacer.setPreferredSize(new java.awt.Dimension(newWidth, 1));
            this.spacer.revalidate();
            this.revalidate();
            this.repaint();
        }
    });
    }

    /**
     * Set a global label width used by all PhaseScoreField instances created
     * after calling this method. This helps align the spinner input across
     * multiple rows so the entry fields start at a consistent position.
     */
    public static void setGlobalLabelWidth(int px) {
        GLOBAL_LABEL_WIDTH_PX = Math.max(80, px);
    }

    private static int computePreferredHeight(Font font, int approxLines) {
        if (font == null) return 40;
        javax.swing.JLabel probe = new javax.swing.JLabel();
        java.awt.FontMetrics fm = probe.getFontMetrics(font);
        int h = fm.getHeight() * Math.max(1, approxLines) + 6;
        return Math.max(40, h);
    }

    public static int getGlobalLabelWidth() { return GLOBAL_LABEL_WIDTH_PX; }

    /**
     * Compute the pixel width of the longest label string using the given
     * font. Returns the maximum string width in pixels.
     */
    public static int computeMaxLabelPixelWidth(java.awt.Font font, String[] labels) {
        if (labels == null || labels.length == 0) return GLOBAL_LABEL_WIDTH_PX;
        javax.swing.JLabel probe = new javax.swing.JLabel();
        java.awt.FontMetrics fm = probe.getFontMetrics(font != null ? font : probe.getFont());
        int max = 0;
        for (String s : labels) if (s != null) max = Math.max(max, fm.stringWidth(s));
        return max;
    }

    public void setLabel(String text) { labelArea.setText(text); }

    public String getLabel() { return labelArea.getText(); }

    public int getValue() { return (Integer) spinner.getValue(); }

    public void setValue(int v) { spinner.setValue(Math.max(0, Math.min(4, v))); }

    @Override
    public void setName(String name) { 
        super.setName(name);
        spinner.setName(name);
    }

    // Diagnostics: expose spinner X and label wrap width (useful to verify layout)
    public int getSpinnerX() { return spinner.getLocation().x; }
    public int getLabelWrapWidth() { return labelWrap.getWidth(); }
    /** Actual horizontal gap in pixels between the label wrap right edge and the spinner left edge. */
    public int getActualGap() {
        int labelRight = labelWrap.getX() + labelWrap.getWidth();
        return spinner.getX() - labelRight;
    }
}
