package com.studentgui.apphelpers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 * Very small non-modal notification window for quick status messages.
 */
public class UiNotifier {
    private static JWindow window;

    public static void show(String message) {
        SwingUtilities.invokeLater(() -> {
            if (window != null) window.dispose();
            window = new JWindow();
            JLabel label = new JLabel(message);
            label.setOpaque(true);
            label.setBackground(new Color(0, 0, 0, 200));
            label.setForeground(Color.WHITE);
            label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            window.getContentPane().setLayout(new BorderLayout());
            window.getContentPane().add(label, BorderLayout.CENTER);
            window.pack();
            window.setAlwaysOnTop(true);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
            // auto-hide after 2 seconds
            new Thread(() -> {
                try { Thread.sleep(2000); }
                catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                SwingUtilities.invokeLater(() -> { if (window != null) { window.dispose(); window = null; } });
            }).start();
        });
    }
}
