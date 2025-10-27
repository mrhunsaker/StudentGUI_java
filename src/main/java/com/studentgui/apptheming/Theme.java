package com.studentgui.apptheming;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.studentgui.app.Main;

/**
 * Application theming helpers (menu and look-and-feel wiring).
 */
/**
 * Small theming and menu helper. Constructs a simple Navigate menu used by
 * the main application window.
 */
public class Theme {
    /**
     * Build and return the application menu bar used in the main frame.
     *
     * @return a {@link JMenuBar} instance containing the application's menus
     */
    public static JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu nav = new JMenu("Navigate");

        // Home
        JMenuItem home = new JMenuItem(new AbstractAction("Home") {
            @Override
            public void actionPerformed(final ActionEvent e) { Main.showPage("homepage", null); }
        });
        home.setMnemonic('H');
        home.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        home.setIcon(makeIcon(new Color(0x4A90E2), 12));
        home.getAccessibleContext().setAccessibleName("Home");
        home.getAccessibleContext().setAccessibleDescription("Open the Home page");
        nav.add(home);
        nav.addSeparator();

        // Tactile section (alphabetical)
        JMenu tactile = new JMenu("Tactile");
        JMenuItem abacus = new JMenuItem(new AbstractAction("Abacus") {
            @Override public void actionPerformed(final ActionEvent e) { Main.showPage("abacus", null); }
        });
        abacus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        abacus.setIcon(makeIcon(new Color(0xF5A623), 12));
        abacus.getAccessibleContext().setAccessibleName("Abacus");
        abacus.getAccessibleContext().setAccessibleDescription("Open the Abacus skills page");
        tactile.add(abacus);

        JMenuItem braille = new JMenuItem(new AbstractAction("Braille") {
            @Override public void actionPerformed(final ActionEvent e) { Main.showPage("braille", null); }
        });
        braille.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        braille.setIcon(makeIcon(new Color(0x50E3C2), 12));
        braille.getAccessibleContext().setAccessibleName("Braille");
        braille.getAccessibleContext().setAccessibleDescription("Open the Braille skills page");
        tactile.add(braille);

        nav.add(tactile);
        nav.addSeparator();

        // Technology section (alphabetical)
        JMenu tech = new JMenu("Technology");
        JMenuItem brailleNote = new JMenuItem(new AbstractAction("BrailleNote Touch") {
            @Override public void actionPerformed(final ActionEvent e) { Main.showPage("braillenote", null); }
        });
        brailleNote.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        brailleNote.setIcon(makeIcon(new Color(0x7B61FF), 12));
        brailleNote.getAccessibleContext().setAccessibleName("BrailleNote Touch");
        brailleNote.getAccessibleContext().setAccessibleDescription("Open the BrailleNote Touch page");
        tech.add(brailleNote);

        JMenuItem brailleSense = new JMenuItem(new AbstractAction("Braille Sense") {
            @Override public void actionPerformed(final ActionEvent e) { Main.showPage("braillesense", null); }
        });
        brailleSense.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        brailleSense.setIcon(makeIcon(new Color(0xF8E71C), 12));
        brailleSense.getAccessibleContext().setAccessibleName("Braille Sense");
        brailleSense.getAccessibleContext().setAccessibleDescription("Open the Braille Sense page");
        tech.add(brailleSense);

        JMenuItem dl = new JMenuItem(new AbstractAction("Digital Literacy") {
            @Override public void actionPerformed(final ActionEvent e) { Main.showPage("digitalliteracy", null); }
        });
        dl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        dl.setIcon(makeIcon(new Color(0x7ED321), 12));
        dl.getAccessibleContext().setAccessibleName("Digital Literacy");
        dl.getAccessibleContext().setAccessibleDescription("Open the Digital Literacy page");
        tech.add(dl);

        JMenuItem ios = new JMenuItem(new AbstractAction("iOS") {
            @Override public void actionPerformed(final ActionEvent e) { Main.showPage("ios", null); }
        });
        ios.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        ios.setIcon(makeIcon(new Color(0x00A5E0), 12));
        ios.getAccessibleContext().setAccessibleName("iOS");
        ios.getAccessibleContext().setAccessibleDescription("Open the iOS accessibility page");
        tech.add(ios);

        JMenuItem keyboarding = new JMenuItem(new AbstractAction("Keyboarding") {
            @Override public void actionPerformed(final ActionEvent e) { Main.showPage("keyboarding", null); }
        });
        keyboarding.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        keyboarding.setIcon(makeIcon(new Color(0x8B572A), 12));
        keyboarding.getAccessibleContext().setAccessibleName("Keyboarding");
        keyboarding.getAccessibleContext().setAccessibleDescription("Open the Keyboarding skills page");
        tech.add(keyboarding);

        JMenuItem screenReader = new JMenuItem(new AbstractAction("Screen Reader") {
            @Override public void actionPerformed(final ActionEvent e) { Main.showPage("screenreader", null); }
        });
        screenReader.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        screenReader.setIcon(makeIcon(new Color(0x417505), 12));
        screenReader.getAccessibleContext().setAccessibleName("Screen Reader");
        screenReader.getAccessibleContext().setAccessibleDescription("Open the Screen Reader page");
        tech.add(screenReader);

        nav.add(tech);
        nav.addSeparator();

        // Misc (alphabetical)
        JMenu misc = new JMenu("Misc");
        JMenuItem contactLog = new JMenuItem(new AbstractAction("Contact Log") {
            @Override public void actionPerformed(final ActionEvent e) { Main.showPage("contactlog", null); }
        });
        contactLog.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        contactLog.setIcon(makeIcon(new Color(0xF18805), 12));
        contactLog.getAccessibleContext().setAccessibleName("Contact Log");
        contactLog.getAccessibleContext().setAccessibleDescription("Open the Contact Log page");
        misc.add(contactLog);

        JMenuItem observations = new JMenuItem(new AbstractAction("Observations") {
            @Override public void actionPerformed(final ActionEvent e) { Main.showPage("observations", null); }
        });
        observations.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        observations.setIcon(makeIcon(new Color(0x50E3C2), 12));
        observations.getAccessibleContext().setAccessibleName("Observations");
        observations.getAccessibleContext().setAccessibleDescription("Open the Observations page");
        misc.add(observations);

        JMenuItem sessionNotes = new JMenuItem(new AbstractAction("Session Notes") {
            @Override public void actionPerformed(final ActionEvent e) { Main.showPage("sessionnotes", null); }
        });
        sessionNotes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        sessionNotes.setIcon(makeIcon(new Color(0xD0021B), 12));
        sessionNotes.getAccessibleContext().setAccessibleName("Session Notes");
        sessionNotes.getAccessibleContext().setAccessibleDescription("Open the Session Notes page");
        misc.add(sessionNotes);

        nav.add(misc);

        mb.add(nav);

        // Themes menu (top-level)
        JMenu themesMenu = new JMenu("Themes");
        // Read persisted theme choice so we can mark the active menu item
        String currentTheme = com.studentgui.apphelpers.Settings.get("theme", "light");

        javax.swing.ButtonGroup themeGroup = new javax.swing.ButtonGroup();

        javax.swing.JRadioButtonMenuItem light = new javax.swing.JRadioButtonMenuItem(new AbstractAction("Light") {
            @Override public void actionPerformed(final ActionEvent e) { Main.setTheme("light"); com.studentgui.apphelpers.Settings.put("theme", "light"); }
        });
        light.setIcon(makeIcon(new Color(0x000000), 12));
        light.getAccessibleContext().setAccessibleName("Light theme");
        light.getAccessibleContext().setAccessibleDescription("Switch to the light theme");
        if ("light".equalsIgnoreCase(currentTheme) || "flatlightlaf".equalsIgnoreCase(currentTheme)) {
            light.setSelected(true);
        }
        themeGroup.add(light);
        themesMenu.add(light);

        javax.swing.JRadioButtonMenuItem dark = new javax.swing.JRadioButtonMenuItem(new AbstractAction("Dark") {
            @Override public void actionPerformed(final ActionEvent e) { Main.setTheme("dark"); com.studentgui.apphelpers.Settings.put("theme", "dark"); }
        });
        dark.setIcon(makeIcon(new Color(0x2C2C2C), 12));
        dark.getAccessibleContext().setAccessibleName("Dark theme");
        dark.getAccessibleContext().setAccessibleDescription("Switch to the dark theme");
        if ("dark".equalsIgnoreCase(currentTheme) || "flatdarklaf".equalsIgnoreCase(currentTheme)) {
            dark.setSelected(true);
        }
        themeGroup.add(dark);
        themesMenu.add(dark);

        javax.swing.JRadioButtonMenuItem intellij = new javax.swing.JRadioButtonMenuItem(new AbstractAction("IntelliJ (Darcula)") {
            @Override public void actionPerformed(final ActionEvent e) { Main.setTheme("darcula"); com.studentgui.apphelpers.Settings.put("theme", "darcula"); }
        });
        intellij.setIcon(makeIcon(new Color(0x4A4A4A), 12));
        intellij.getAccessibleContext().setAccessibleName("IntelliJ Darcula");
        intellij.getAccessibleContext().setAccessibleDescription("Switch to the IntelliJ Darcula theme");
        if ("darcula".equalsIgnoreCase(currentTheme)) {
            intellij.setSelected(true);
        }
        themeGroup.add(intellij);
        themesMenu.add(intellij);
        themesMenu.addSeparator();

        // Dynamically add all IntelliJ themes available from flatlaf-intellij-themes
        // Discover and add IntelliJ themes from the flatlaf-intellij-themes artifact if present
        List<String> intellijThemes = listClassesInPackage("com.formdev.flatlaf.intellijthemes");
        if (!intellijThemes.isEmpty()) {
            JMenu intellijGroup = new JMenu("IntelliJ Themes");
            for (String cls : intellijThemes) {
                final String className = cls;
                    JMenuItem mi = new JMenuItem(new AbstractAction(simpleName(className)) {
                    @Override public void actionPerformed(final ActionEvent e) { Main.setTheme(className); com.studentgui.apphelpers.Settings.put("theme", className); }
                });
                mi.setIcon(makeIcon(new Color(0x888888), 10));
                mi.getAccessibleContext().setAccessibleName(className);
                mi.getAccessibleContext().setAccessibleDescription("Apply " + className);
                intellijGroup.add(mi);
            }
            themesMenu.add(intellijGroup);
        }

        // Material themes: if user adds flatlaf-themes or material themes library, we can try to load them by class name
        JMenu materialGroup = new JMenu("Material Themes");
        List<String> materialThemes = listClassesInPackage("com.formdev.flatlaf.materialthemes");
        for (String cls : materialThemes) {
            final String className = cls;
            JMenuItem mi = new JMenuItem(new AbstractAction(simpleName(className)) {
                @Override public void actionPerformed(final ActionEvent e) { Main.setTheme(className); com.studentgui.apphelpers.Settings.put("theme", className); }
            });
            mi.setIcon(makeIcon(new Color(0x666666), 10));
            mi.getAccessibleContext().setAccessibleName(className);
            mi.getAccessibleContext().setAccessibleDescription("Apply " + className);
            materialGroup.add(mi);
        }
        themesMenu.add(materialGroup);

        mb.add(themesMenu);
        return mb;
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Theme() {
        throw new AssertionError("Not instantiable");
    }

    /**
     * Create a small square color icon used for menu items. Kept local to avoid
     * needing external resources; a simple filled rounded rectangle is drawn.
     */
    private static ImageIcon makeIcon(final Color color, final int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.fillRoundRect(0, 0, size, size, Math.max(2, size/4), Math.max(2, size/4));
        } finally {
            g.dispose();
        }
        return new ImageIcon(img);
    }

    // Return the simple class name from a fully-qualified class name
    private static String simpleName(final String fqcn) {
        int idx = fqcn.lastIndexOf('.');
        return idx >= 0 ? fqcn.substring(idx + 1) : fqcn;
    }

    // List classes in a package by scanning classpath entries. This is a best-effort
    // method: it handles classes inside jars and on the filesystem.
    private static List<String> listClassesInPackage(final String packageName) {
        List<String> results = new ArrayList<>();
        String path = packageName.replace('.', '/');
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = cl.getResources(path);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                URLConnection conn = url.openConnection();
                if (conn instanceof JarURLConnection) {
                    JarURLConnection juc = (JarURLConnection) conn;
                    try (JarFile jar = juc.getJarFile()) {
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry je = entries.nextElement();
                            String name = je.getName();
                            if (name.startsWith(path) && name.endsWith(".class") && !je.isDirectory()) {
                                String cls = name.replace('/', '.').replaceAll("\\.class$", "");
                                results.add(cls);
                            }
                        }
                    }
                } else {
                    try {
                        URI uri = url.toURI();
                        File folder = new File(uri);
                        if (folder.isDirectory()) {
                            File[] files = folder.listFiles();
                            if (files != null) {
                                for (File f : files) {
                                    if (f.isFile() && f.getName().endsWith(".class")) {
                                        String cls = packageName + "." + f.getName().replaceAll("\\.class$", "");
                                        results.add(cls);
                                    }
                                }
                            }
                        }
                    } catch (java.net.URISyntaxException ioe) {
                        // ignore
                    }
                }
            }
        } catch (IOException e) {
            // ignore
        }
        return results;
    }
}
