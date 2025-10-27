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
    // current date used by the top bar (can be updated without recreating pages)
    private static java.time.LocalDate currentDate;
    private static String currentStudent;
    // Listeners to notify when the top-bar date changes
    private static final java.util.List<DateChangeListener> dateListeners = new java.util.concurrent.CopyOnWriteArrayList<>();

    /**
     * Register a listener to be notified when the application date is changed via the top bar.
     *
     * @param l listener to register (ignored when null)
     */
    public static void addDateChangeListener(final DateChangeListener l) { 
        if (l != null) {
            dateListeners.add(l);
        }
    }

    /**
     * Remove a previously registered date change listener.
     *
     * @param l listener to remove (ignored when null)
     */
    public static void removeDateChangeListener(final DateChangeListener l) { 
        if (l != null) {
            dateListeners.remove(l);
        }
    }

    /**
     * Clear all registered date change listeners.
     */
    public static void clearDateChangeListeners() { 
        dateListeners.clear();
    }

    /**
     * Notify all registered date listeners that the application date has changed.
     *
     * @param d new application date
     */
    private static void notifyDateChanged(final java.time.LocalDate d) {
        for (DateChangeListener l : dateListeners) {
            try {
                l.dateChanged(d);
            } catch (Exception ex) {
                LOG.warn("DateChangeListener threw: {}", ex.toString());
            }
        }
    }
    // Student change listeners
    private static final java.util.List<StudentChangeListener> studentListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    /**
     * Register a listener to be notified when the selected student is changed.
     *
     * @param l listener to register (ignored when null)
     */
    public static void addStudentChangeListener(final StudentChangeListener l) {
        if (l != null) {
            studentListeners.add(l);
        }
    }

    /**
     * Remove a previously registered student change listener.
     *
     * @param l listener to remove (ignored when null)
     */
    public static void removeStudentChangeListener(final StudentChangeListener l) {
        if (l != null) {
            studentListeners.remove(l);
        }
    }

    /**
     * Clear all registered student change listeners.
     */
    public static void clearStudentChangeListeners() {
        studentListeners.clear();
    }

    /**
     * Notify registered student change listeners that the selected student has changed.
     *
     * @param s new selected student name
     */
    private static void notifyStudentChanged(final String s) {
        currentStudent = s;
        for (StudentChangeListener l : studentListeners) {
            try {
                l.studentChanged(s);
            } catch (Exception ex) {
                LOG.warn("StudentChangeListener threw: {}", ex.toString());
            }
        }
    }


    // Settings change listeners
    private static final java.util.List<SettingsChangeListener> settingsListeners = new java.util.concurrent.CopyOnWriteArrayList<>();

    public static void addSettingsChangeListener(final SettingsChangeListener l) {
        if (l != null) {
            settingsListeners.add(l);
        }
    }

    public static void removeSettingsChangeListener(final SettingsChangeListener l) {
        if (l != null) {
            settingsListeners.remove(l);
        }
    }

    public static void clearSettingsChangeListeners() {
        settingsListeners.clear();
    }

    public static void notifySettingsChanged() {
        for (SettingsChangeListener l : settingsListeners) {
            try {
                l.settingsChanged();
            } catch (Exception ex) {
                LOG.warn("SettingsChangeListener threw: {}", ex.toString());
            }
        }
    }

    /**
     * Application entry point. Initializes helpers, database, and launches the
     * Swing UI on the EDT.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(final String[] args) {
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
            if (themeBar == null) {
                themeBar = new javax.swing.JMenuBar();
            }
            javax.swing.JMenu fileMenu = new javax.swing.JMenu("File");
            javax.swing.JMenuItem exitItem = new javax.swing.JMenuItem("Exit");
            exitItem.addActionListener(e -> {
                LOG.info("Exit requested via File->Exit");
                if (frame != null) {
                    frame.dispose();
                }
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
            // Register shared graph to receive settings change notifications
            addSettingsChangeListener(sharedGraph);
            List<String> students = Helpers.getStudents();
            String demoStudent = students.isEmpty() ? "Demo Student" : students.get(0);
            LocalDate today = LocalDate.now();
            currentDate = today;
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
    public static void setTheme(final String theme) {
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
            // Update the app's current date and selected student without recreating pages; show a confirmation dialog.
            currentDate = date;
            currentStudent = selected;
            javax.swing.JOptionPane.showMessageDialog(frame,
                    "The date has been updated to " + date.toString(),
                    "Date Updated",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            // Notify registered pages so they can update any internal state
            notifyDateChanged(date);
            notifyStudentChanged(selected);
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
    private static void recreatePages(final String student, final LocalDate date) {
        // recreate the pages with a fresh sharedGraph so the graph is reset for the selected student/date
        if (sharedGraph == null) {
            sharedGraph = new JLineGraph();
        } else {
            sharedGraph = new JLineGraph();
        }

    // Clear any previous listeners to avoid stale references
    clearDateChangeListeners();
    clearStudentChangeListeners();

        contentPanel.removeAll();
        contentPanel.add(Homepage.create(), "homepage");

        // Instantiate pages into locals so we can register listeners if they implement the interface
        Braille braille = new Braille(student, date, sharedGraph);
        contentPanel.add(braille, "braille");
    if (braille instanceof DateChangeListener d) {
        addDateChangeListener(d);
    }
    if (braille instanceof StudentChangeListener s) {
        addStudentChangeListener(s);
    }

        Abacus abacus = new Abacus(student, date, sharedGraph);
        contentPanel.add(abacus, "abacus");
    if (abacus instanceof DateChangeListener d2) {
        addDateChangeListener(d2);
    }
    if (abacus instanceof StudentChangeListener s2) {
        addStudentChangeListener(s2);
    }

        BrailleNote brailleNote = new BrailleNote(student, date, sharedGraph);
        contentPanel.add(brailleNote, "braillenote");
    if (brailleNote instanceof DateChangeListener d3) {
        addDateChangeListener(d3);
    }
    if (brailleNote instanceof StudentChangeListener s3) {
        addStudentChangeListener(s3);
    }

        DigitalLiteracy dl = new DigitalLiteracy(student, date, sharedGraph);
        contentPanel.add(dl, "digitalliteracy");
    if (dl instanceof DateChangeListener d4) {
        addDateChangeListener(d4);
    }
    if (dl instanceof StudentChangeListener s4) {
        addStudentChangeListener(s4);
    }

        // pages that don't currently need date-driven updates remain created inline
        contentPanel.add(new BrailleSense(student, date, sharedGraph), "braillesense");
        contentPanel.add(new CVI(student, date, sharedGraph), "cvi");

        IOS ios = new IOS(student, date, sharedGraph);
        contentPanel.add(ios, "ios");
    if (ios instanceof DateChangeListener d5) {
        addDateChangeListener(d5);
    }
    if (ios instanceof StudentChangeListener s5) {
        addStudentChangeListener(s5);
    }

        Keyboarding keyboarding = new Keyboarding(student, date, sharedGraph);
        contentPanel.add(keyboarding, "keyboarding");
    if (keyboarding instanceof DateChangeListener d6) {
        addDateChangeListener(d6);
    }
    if (keyboarding instanceof StudentChangeListener s6) {
        addStudentChangeListener(s6);
    }

        contentPanel.add(new Observations(student, date), "observations");

        ScreenReader sr = new ScreenReader(student, date, sharedGraph);
        contentPanel.add(sr, "screenreader");
    if (sr instanceof DateChangeListener d7) {
        addDateChangeListener(d7);
    }
    if (sr instanceof StudentChangeListener s7) {
        addStudentChangeListener(s7);
    }

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
    public static void showPage(final String name, final JComponent comp) {
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
