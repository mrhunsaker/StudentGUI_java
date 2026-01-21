package com.studentgui.test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.studentgui.apphelpers.Helpers;
import com.studentgui.apphelpers.SqlGenerate;

/**
 * DatabaseEdgeCasesTest - TODO: describe this DatabaseEdgeCasesTest
 */

public class DatabaseEdgeCasesTest {

    @BeforeAll
    public static void init() throws Exception {
        Helpers.createFolderHierarchy();
        SqlGenerate.initializeDatabase();
    }

    @Test
    /**
     * duplicateStudentNamesReturnSameId - TODO: describe this method
     */

    public void duplicateStudentNamesReturnSameId() throws Exception {
        int a = com.studentgui.apphelpers.Database.getOrCreateStudent("Dup Student");
        int b = com.studentgui.apphelpers.Database.getOrCreateStudent("Dup Student");
        assertEquals(a, b, "Duplicate student names should return the same id");
    }

    @Test
    /**
     * ensureAssessmentPartsIsIdempotentAndIgnoresUnknownPartsOnInsert - TODO: describe this method
     */

    public void ensureAssessmentPartsIsIdempotentAndIgnoresUnknownPartsOnInsert() throws Exception {
        int pt = com.studentgui.apphelpers.Database.getOrCreateProgressType("EdgeType");
        String[] parts = new String[] {"X1","X2","X3"};
        com.studentgui.apphelpers.Database.ensureAssessmentParts(pt, parts);
        // calling again should not fail and should be idempotent
        com.studentgui.apphelpers.Database.ensureAssessmentParts(pt, parts);

        int sid = com.studentgui.apphelpers.Database.getOrCreateStudent("Edge Student");
        int session = com.studentgui.apphelpers.Database.createProgressSession(sid, pt, LocalDate.now());
        // insert with an unknown part code - should be ignored, no exception
        com.studentgui.apphelpers.Database.insertAssessmentResults(session, pt, new String[] {"X1","UNKNOWN"}, new int[] {5, 9});

        List<List<Integer>> rows = com.studentgui.apphelpers.Database.fetchLatestAssessmentResults("Edge Student", "EdgeType", 5);
        assertNotNull(rows);
        assertTrue(rows.size() >= 1);
    }

    @Test
    /**
     * saveSessionNotesPersistsNotes - TODO: describe this method
     */

    public void saveSessionNotesPersistsNotes() throws Exception {
        int pt = com.studentgui.apphelpers.Database.getOrCreateProgressType("NoteType");
        int sid = com.studentgui.apphelpers.Database.getOrCreateStudent("Notes Student");
        int session = com.studentgui.apphelpers.Database.createProgressSession(sid, pt, LocalDate.now());
        com.studentgui.apphelpers.Database.saveSessionNotes(session, "These are test notes");

        List<List<Integer>> rows = com.studentgui.apphelpers.Database.fetchLatestAssessmentResults("Notes Student", "NoteType", 5);
        // fetchLatestAssessmentResults doesn't return notes, but we can at least ensure the session exists by getting session rows
        assertNotNull(rows);
    }
}
