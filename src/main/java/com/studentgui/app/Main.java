package com.studentgui.app;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.studentgui.apphelpers.Helpers;
import com.studentgui.apphelpers.SqlGenerate;
import com.studentgui.apppages.Abacus;
import com.studentgui.apppages.Braille;
import com.studentgui.apppages.BrailleNote;
import com.studentgui.apppages.BrailleSense;
import com.studentgui.apppages.CVI;
import com.studentgui.apppages.ContactLog;
import com.studentgui.apppages.DigitalLiteracy;
import com.studentgui.apppages.Homepage;
import com.studentgui.apppages.IOS;
import com.studentgui.apppages.InstructionalMaterials;
import com.studentgui.apppages.JLineGraph;
import com.studentgui.apppages.Keyboarding;
import com.studentgui.apppages.Observations;
import com.studentgui.apppages.ScreenReader;
import com.studentgui.apppages.SessionNotes;
import com.studentgui.apptheming.Theme;

/**
 * Main application entry and UI wiring for the Student Skills Progressions app.
 *
 * This class builds the top-level window, menu, and registers the skill pages
 * (each page is a JPanel). It's intentionally lightweight; most functionality
 * for database access and page logic lives in helper classes and the page
 * components under com.studentgui.apppages.
 */
/**
 * Application bootstrap and top-level UI wiring. Builds the main JFrame,
 * registers pages, and provides a small top control bar for switching
 * students and pages.
 */
/**
 * Application bootstrap and top-level UI wiring. Builds the main JFrame,
 * registers pages, and provides a small top control bar for switching
 * students and pages.
 */
/**
 * Application entry point and top-level UI wiring for the Student Skills
 * Progressions application. Builds the main frame, menu and registers per-page
 * panels under a CardLayout.
 */
public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static JFrame frame;
    private static JPanel contentPanel;
    private static JLineGraph sharedGraph;

    /**
     * Application entry point. Initializes helpers, database, and launches the
     * Swing UI on the EDT.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        // Apply saved look and feel (default to light)
        // Settings.get and setTheme handle any expected failures internally;
        // call directly so we avoid a broad RuntimeException catch.
        String saved = com.studentgui.apphelpers.Settings.get("theme", "light");
        setTheme(saved);

        // Initialize helpers and DB
        Helpers.setStartDir();
        Helpers.createFolderHierarchy();
        SqlGenerate.initializeDatabase();

        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Student Skills Progressions");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);

            // Menu bar: obtain the app menu bar from Theme, insert a File->Exit menu at the far left
            javax.swing.JMenuBar themeBar = Theme.createMenuBar();
            if (themeBar == null) themeBar = new javax.swing.JMenuBar();
            javax.swing.JMenu fileMenu = new javax.swing.JMenu("File");
            javax.swing.JMenuItem exitItem = new javax.swing.JMenuItem("Exit");
            exitItem.addActionListener(e -> {
                LOG.info("Exit requested via File->Exit");
                if (frame != null) frame.dispose();
                System.exit(0);
            });
            fileMenu.add(exitItem);
            // Insert file menu at position 0 so it appears on the far left
            themeBar.add(fileMenu, 0);
            // Ensure the Themes menu (if present) appears immediately after File
            int themesIdx = -1;
            for (int i = 0; i < themeBar.getMenuCount(); i++) {
                javax.swing.JMenu m = themeBar.getMenu(i);
                if (m != null && "Themes".equals(m.getText())) { themesIdx = i; break; }
            }
            if (themesIdx > 1) {
                javax.swing.JMenu themesMenu = themeBar.getMenu(themesIdx);
                themeBar.remove(themesIdx);
                themeBar.add(themesMenu, 1);
            }
            frame.setJMenuBar(themeBar);


            contentPanel = new JPanel(new CardLayout());
            frame.add(contentPanel, BorderLayout.CENTER);

            // Top control bar: student selector, date, and navigation
            JPanel topBar = buildTopBar();
            frame.add(topBar, BorderLayout.NORTH);

            // Create initial shared graph and pages for the first student
            sharedGraph = new JLineGraph();
            List<String> students = Helpers.getStudents();
            String demoStudent = students.isEmpty() ? "Demo Student" : students.get(0);
            LocalDate today = LocalDate.now();
            recreatePages(demoStudent, today);

            frame.setVisible(true);
        });
    }

    /**
     * Change application theme at runtime. Supported values: "light", "dark", "darcula".
     * This method updates the installed Look and Feel and refreshes the main frame.
    *
    * @param theme human-friendly theme name or fully-qualified LookAndFeel class name
     */
    public static void setTheme(String theme) {
        try {
            String t = theme == null ? "light" : theme;
            // Common keywords for bundled themes
            switch (t.toLowerCase()) {
                case "dark":
                case "flatdarklaf":
                    UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
                    break;
                case "darcula":
                    // Darcula-like: use FlatDarkLaf as fallback
                    UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
                    break;
                case "light":
                case "flatlightlaf":
                    UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
                    break;
                default:
                    // If the string looks like a fully-qualified class name, try to set it directly.
                    if (t.contains(".")) {
                        try {
                            UIManager.setLookAndFeel(t);
                        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
                            // Try to instantiate via reflection
                            try {
                                Class<?> c = Class.forName(t);
                                Object o = c.getDeclaredConstructor().newInstance();
                                if (o instanceof javax.swing.LookAndFeel) {
                                    UIManager.setLookAndFeel((javax.swing.LookAndFeel) o);
                                } else {
                                    // fallback to light
                                    UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
                                }
                            } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex2) {
                                LOG.error("Failed to set look and feel by class name {}", t, ex2);
                                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
                            }
                        }
                    } else {
                        // Try to find an installed LAF by name
                        boolean applied = false;
                        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                            if (info.getName().equalsIgnoreCase(t) || info.getName().toLowerCase().contains(t.toLowerCase())) {
                                UIManager.setLookAndFeel(info.getClassName());
                                applied = true;
                                break;
                            }
                        }
                        if (!applied) {
                            // default fallback
                            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
                        }
                    }
                    break;
            }
            if (frame != null) {
                javax.swing.SwingUtilities.updateComponentTreeUI(frame);
                frame.pack();
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException | IllegalArgumentException e) {
            LOG.error("Failed to set theme {}", theme, e);
        }
    }

    private static JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        List<String> students = Helpers.getStudents();
        JComboBox<String> studentBox = new JComboBox<>(students.toArray(new String[0]));
        studentBox.setEditable(false);

        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD):");
        JTextField dateField = new JTextField(LocalDate.now().toString(), 10);

        JButton goBtn = new JButton("Apply");

        bar.add(new JLabel("Student:"));
        bar.add(studentBox);
        bar.add(dateLabel);
        bar.add(dateField);
        bar.add(goBtn);

        goBtn.addActionListener(e -> {
            String selected = (String) studentBox.getSelectedItem();
            LocalDate date = LocalDate.now();
            try {
                date = LocalDate.parse(dateField.getText());
            } catch (DateTimeParseException ex) {
                // keep today
            }
            recreatePages(selected, date);
        });

    // Navigation buttons removed from top bar per UI request; pages can still be selected via menu

        return bar;
    }

    /**
     * Recreate per-page panels for the provided student and date. This replaces
     * the CardLayout content so the shared graph and pages are reset.
     */
    /**
     * Recreate per-page panels for the provided student and date. This
     * replaces the CardLayout content so the shared graph and pages are reset.
     *
     * @param student selected student's display name
     * @param date the session date for newly created pages
     */
    private static void recreatePages(String student, LocalDate date) {
        // recreate the pages with a fresh sharedGraph so the graph is reset for the selected student/date
        if (sharedGraph == null) sharedGraph = new JLineGraph();
        else sharedGraph = new JLineGraph();

        contentPanel.removeAll();
        contentPanel.add(Homepage.create(), "homepage");
        contentPanel.add(new Braille(student, date, sharedGraph), "braille");
        contentPanel.add(new Abacus(student, date, sharedGraph), "abacus");
        contentPanel.add(new BrailleNote(student, date, sharedGraph), "braillenote");
        contentPanel.add(new DigitalLiteracy(student, date, sharedGraph), "digitalliteracy");
    contentPanel.add(new BrailleSense(student, date, sharedGraph), "braillesense");
    contentPanel.add(new CVI(student, date, sharedGraph), "cvi");
    contentPanel.add(new IOS(student, date, sharedGraph), "ios");
    contentPanel.add(new Keyboarding(student, date, sharedGraph), "keyboarding");
    contentPanel.add(new Observations(student, date, sharedGraph), "observations");
    contentPanel.add(new ScreenReader(student, date, sharedGraph), "screenreader");
    contentPanel.add(new SessionNotes(student, date, sharedGraph), "sessionnotes");
    contentPanel.add(new ContactLog(student, date, sharedGraph), "contactlog");
    contentPanel.add(new InstructionalMaterials(), "instructionalmaterials");

        contentPanel.revalidate();
        contentPanel.repaint();
        showPage("homepage", null);
    }

    /**
     * Show a page previously registered with the CardLayout. If a component
     * is provided and not yet added it will be registered under the given name.
     *
     * @param name registration name for the page
     * @param comp optional component instance to add (may be null)
     */
    public static void showPage(String name, JComponent comp) {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        if (comp != null && comp.getParent() == null) {
            contentPanel.add(comp, name);
        }
        cl.show(contentPanel, name);
    }

    /**
     * Private constructor to prevent instantiation of this utility/entry class.
     */
    private Main() {
        throw new AssertionError("Not instantiable");
    }
}
