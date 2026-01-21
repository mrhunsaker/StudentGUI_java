package com.studentgui.test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.studentgui.apphelpers.Helpers;
import com.studentgui.apphelpers.SqlGenerate;

/**
 * Small integration-style unit test that uses the normalized Database helper methods
 * to create a student, a progress type, ensure parts, insert one session and fetch the
 * latest results. This runs headless and doesn't start any UI components.
 */
public class BrailleDatabaseTest {

    @Test
    /**
     * Exercise a simple database create/save/fetch flow for Braille session
     * records and assert basic invariants to detect regressions.
     */

    public void smokeDatabaseFlow() throws Exception {
        // Ensure app folders and DB exist
        Helpers.createFolderHierarchy();
        SqlGenerate.initializeDatabase();

        int studentId = com.studentgui.apphelpers.Database.getOrCreateStudent("JUnit Smoke Student");
        assertTrue(studentId > 0);

        int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("Braille");
        assertTrue(ptId > 0);

        String[] codes = new String[5];
        int[] scores = new int[5];
        for (int i = 0; i < 5; i++) { codes[i] = "P" + (i+1); scores[i] = (i % 3) + 1; }
        com.studentgui.apphelpers.Database.ensureAssessmentParts(ptId, codes);

        int sessionId = com.studentgui.apphelpers.Database.createProgressSession(studentId, ptId, LocalDate.now());
        assertTrue(sessionId > 0);

        com.studentgui.apphelpers.Database.insertAssessmentResults(sessionId, ptId, codes, scores);

        List<List<Integer>> rows = com.studentgui.apphelpers.Database.fetchLatestAssessmentResults("JUnit Smoke Student", "Braille", 5);
        assertNotNull(rows);

        // At least one row should be returned
        assertTrue(rows.size() >= 1);
    }
}
