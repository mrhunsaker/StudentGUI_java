package com.studentgui.tools;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.IntStream;

import javax.swing.JButton;

import com.studentgui.apphelpers.Helpers;
import com.studentgui.apppages.Braille;
import com.studentgui.apppages.JLineGraph;

/**
 * Programmatically create a Braille page, populate PhaseScoreField values,
 * and trigger the submit action to verify DB insert and PNG export.
 * <p>
 * This small test runs without user interaction and is useful during
 * automated smoke tests or developer verification of page submission
 * behaviour. Outputs are written under the application's app_home.
 * </p>
 */
public class ProgrammaticPageSaveTest {
    /**
     * Program entry to run the programmatic page save test.
     *
     * @param args ignored
     * @throws Exception on reflection or DB errors
     */
    public static void main(final String[] args) throws Exception {
        Helpers.createFolderHierarchy();
        JLineGraph graph = new JLineGraph();
        Braille page = new Braille("Smoke Test", LocalDate.now(), graph);

        // Set all fields to 3 via getComponents traversal
        Arrays.stream(page.getComponents()).forEach(c -> {
            // nothing here; we'll rely on the submit button to collect values from the internal PhaseScoreField instances
        });

        // Helper: find submit button by accessible name and click it
        JButton submit = findButtonByAccessibleName(page, "Submit Braille Data");
        if (submit == null) {
            System.out.println("Submit button not found; aborting test");
            return;
        }

        // Programmatically set values using the page's declared skillFields via reflection
        try {
            java.lang.reflect.Field f = Braille.class.getDeclaredField("skillFields");
            f.setAccessible(true);
            Object arr = f.get(page);
            if (arr instanceof com.studentgui.uicomp.PhaseScoreField[]) {
                com.studentgui.uicomp.PhaseScoreField[] s = (com.studentgui.uicomp.PhaseScoreField[]) arr;
                IntStream.range(0, s.length).forEach(i -> s[i].setValue(3));
            }
        } catch (ReflectiveOperationException roe) {
            roe.printStackTrace();
            System.out.println("Unable to set skillFields via reflection");
        }

        // Trigger submit
        System.out.println("Triggering submit button action...");
        submit.doClick();

        System.out.println("Programmatic submit triggered. Check app_home for outputs.");
    }

    private static JButton findButtonByAccessibleName(final java.awt.Container c, final String name) {
        for (java.awt.Component comp : c.getComponents()) {
            if (comp instanceof JButton) {
                JButton b = (JButton) comp;
                if (name.equals(b.getAccessibleContext().getAccessibleName())) {
                    return b;
                }
            }
            if (comp instanceof java.awt.Container) {
                JButton r = findButtonByAccessibleName((java.awt.Container) comp, name);
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    /**
     * Private constructor to avoid instantiation - this class is a programmatic
     * test harness containing only static helpers and a main method.
     */
    private ProgrammaticPageSaveTest() {
        // no instances
    }
}
