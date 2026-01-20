package com.studentgui.app;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.studentgui.apphelpers.Settings;

/**
 * Simple modal preferences dialog exposing a few runtime toggles that
 * affect chart rendering. Preferences are persisted via
 * {@link com.studentgui.apphelpers.Settings} and listeners are notified
 * through {@link Main#notifySettingsChanged()}.
 */
public final class PreferencesDialog {
    private PreferencesDialog() { throw new AssertionError(); }

    /**
     * Show the modal preferences dialog. The dialog persists changes to
     * {@link com.studentgui.apphelpers.Settings} and notifies runtime
     * listeners via {@link Main#notifySettingsChanged()}.
     *
     * @param owner optional parent frame for dialog positioning
     */
    public static void showDialog(final Frame owner) {
        final JDialog dlg = new JDialog(owner, "Preferences", true);
        dlg.setLayout(new BorderLayout());

        JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT));
        boolean jitterEnabled = Boolean.parseBoolean(Settings.get("jitter.enabled", "true"));
        boolean deterministic = Boolean.parseBoolean(Settings.get("jitter.deterministic", "false"));
        String seed = Settings.get("jitter.seed", "");
    boolean dumpsEnabled = Boolean.parseBoolean(Settings.get("dump.enabled", "false"));

        final JCheckBox jitterCb = new JCheckBox("Enable jitter", jitterEnabled);
        final JCheckBox detCb = new JCheckBox("Deterministic (seeded)", deterministic);
        final JTextField seedField = new JTextField(seed == null ? "" : seed, 12);
    final JCheckBox dumpsCb = new JCheckBox("Enable per-page data dumps", dumpsEnabled);

        center.add(jitterCb);
        center.add(detCb);
    center.add(dumpsCb);
        JLabel seedLabel = new JLabel("Seed:");
        seedLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        center.add(seedLabel);
        center.add(seedField);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");

        save.addActionListener(e -> {
            Settings.put("jitter.enabled", String.valueOf(jitterCb.isSelected()));
            Settings.put("jitter.deterministic", String.valueOf(detCb.isSelected()));
            Settings.put("jitter.seed", seedField.getText().trim());
            Settings.put("dump.enabled", String.valueOf(dumpsCb.isSelected()));
            // notify runtime listeners
            Main.notifySettingsChanged();
            dlg.dispose();
        });

        cancel.addActionListener(e -> dlg.dispose());
        south.add(cancel);
        south.add(save);

        dlg.add(center, BorderLayout.CENTER);
        dlg.add(south, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }
}
