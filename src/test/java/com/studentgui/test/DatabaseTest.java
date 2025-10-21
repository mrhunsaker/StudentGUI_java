package com.studentgui.test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.studentgui.apphelpers.Helpers;
import com.studentgui.apphelpers.SqlGenerate;

/**
 * Basic integration tests for the Database helper using the on-disk sqlite
 * created in the project's application data folder. These tests are small and
 * intentionally exercise CRUD paths used by the UI pages.
 */
public class DatabaseTest {

    @BeforeAll
    public static void init() throws Exception {
        Helpers.createFolderHierarchy();
        SqlGenerate.initializeDatabase();
    }

    @Test
    public void testStudentCreateAndFetch() throws Exception {
        int sid = com.studentgui.apphelpers.Database.getOrCreateStudent("Test Student A");
        assertTrue(sid > 0);

        int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("TestType");
        assertTrue(ptId > 0);

        String[] parts = new String[] {"P1","P2","P3"};
        com.studentgui.apphelpers.Database.ensureAssessmentParts(ptId, parts);

        int sessionId = com.studentgui.apphelpers.Database.createProgressSession(sid, ptId, LocalDate.now());
        assertTrue(sessionId > 0);

        int[] scores = new int[] {1,2,3};
        com.studentgui.apphelpers.Database.insertAssessmentResults(sessionId, ptId, parts, scores);

        List<List<Integer>> results = com.studentgui.apphelpers.Database.fetchLatestAssessmentResults("Test Student A", "TestType", 5);
        assertNotNull(results);
        assertTrue(results.size() >= 1);
    }
}
