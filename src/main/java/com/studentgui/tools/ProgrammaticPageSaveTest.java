package com.studentgui.tools;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.IntStream;

import javax.swing.JButton;

import com.studentgui.apphelpers.Helpers;
import com.studentgui.apppages.Braille;
import com.studentgui.apppages.JLineGraph;

/**
 * Automated integration test for programmatic page manipulation and database submission.
 *
 * <p>Simulates user interaction with the {@link Braille} assessment page by:</p>
 * <ol>
 *   <li>Programmatically instantiating a Braille page with synthetic student/date</li>
 *   <li>Using reflection to access and populate internal {@code PhaseScoreField} components</li>
 *   <li>Locating the "Submit Braille Data" button via accessible name</li>
 *   <li>Programmatically triggering the submit action via {@link JButton#doClick()}</li>
 * </ol>
 *
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Validates end-to-end page submission workflow without GUI interaction</li>
 *   <li>Tests database insert, JSON export, and PNG chart generation in automated context</li>
 *   <li>Verifies reflection-based access to page internals for testing purposes</li>
 *   <li>Provides reference for programmatic testing of other assessment pages</li>
 * </ul>
 *
 * <p><b>Usage:</b></p>
 * <pre>{@code
 * java -cp StudentDataGUI.jar com.studentgui.tools.ProgrammaticPageSaveTest
 * }</pre>
 *
 * <p><b>Expected Side Effects:</b></p>
 * <ul>
 *   <li>New Braille progress session inserted into database for student "Smoke Test"</li>
 *   <li>JSON export written to {@code StudentDataFiles/Smoke_Test/Sessions/Braille/}</li>
 *   <li>Phase-grouped PNG plots written to {@code StudentDataFiles/Smoke_Test/plots/}</li>
 *   <li>Markdown and HTML reports generated in {@code StudentDataFiles/Smoke_Test/reports/}</li>
 * </ul>
 *
 * <p><b>Reflection Usage:</b> Accesses private {@code skillFields} array in {@link Braille}
 * to set all 64 Braille skills to a score of 3. This demonstrates how to programmatically
 * manipulate page state for testing when public setters are not available.</p>
 *
 * <p><b>Validation:</b> After execution, inspect:</p>
 * <ul>
 *   <li>Database: {@code sqlite3 app_home/StudentDatabase/students.db "SELECT * FROM ProgressSession ORDER BY id DESC LIMIT 1;"}</li>
 *   <li>JSON exports: {@code ls -lt app_home/StudentDataFiles/Smoke_Test/Sessions/Braille/}</li>
 *   <li>Generated plots: {@code ls -lt app_home/StudentDataFiles/Smoke_Test/plots/}</li>
 * </ul>
 *
 * <p><b>Note:</b> This test modifies the live database. Run in a test environment or
 * use a separate APP_HOME directory to avoid polluting production data.</p>
 *
 * @see com.studentgui.apppages.Braille
 * @see com.studentgui.uicomp.PhaseScoreField
 * @see javax.swing.JButton#doClick()
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
