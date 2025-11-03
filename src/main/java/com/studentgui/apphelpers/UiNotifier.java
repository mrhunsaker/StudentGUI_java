package com.studentgui.apphelpers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 * Very small non-modal notification window for quick status messages.
 *
 * Lightweight utility used across pages to display transient, non-blocking
 * notifications to the user.
 */
public class UiNotifier {
    private static JWindow window;
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UiNotifier.class);

    /**
     * Display a short, transient notification message on screen.
     *
     * @param message message text to display
     */
    public static void show(final String message) {
        SwingUtilities.invokeLater(() -> {
            if (window != null) {
                window.dispose();
            }
            window = new JWindow();
            JLabel label = new JLabel(message);
            label.setOpaque(true);
            label.setBackground(new Color(0x22, 0x22, 0x22, 200));
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
                catch (InterruptedException ie) { LOG.debug("UiNotifier sleep interrupted", ie); Thread.currentThread().interrupt(); }
                SwingUtilities.invokeLater(() -> { if (window != null) { window.dispose(); window = null; } });
            }).start();
        });
    }
    
    // Note: UiNotifier.show is intentionally lightweight and non-blocking;
    // the implemented method above contains the behavior and JavaDoc.

    /**
     * Private constructor to prevent instantiation.
     */
    private UiNotifier() {
        // utility only
    }
}
