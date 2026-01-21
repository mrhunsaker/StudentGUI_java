package com.studentgui.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.studentgui.apphelpers.Helpers;
import com.studentgui.apphelpers.SqlGenerate;
import com.studentgui.apppages.JLineGraph;

/**
 * JUnit replacement for the legacy Braille smoke main. Exercises the
 * normalized database APIs and invokes JLineGraph.updateWithData(...) to
 * verify plumbing without launching the full GUI.
 */
public class BrailleSmokeTest {

    @Test
    /**
     * smokeTestDatabaseAndGraph - TODO: describe this method
     */

    public void smokeTestDatabaseAndGraph() throws Exception {
        Helpers.createFolderHierarchy();
        SqlGenerate.initializeDatabase();

        int studentId = com.studentgui.apphelpers.Database.getOrCreateStudent("JUnit Smoke Student");
        int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("Braille");

        String[] codes = new String[28];
        int[] scores = new int[28];
        for (int i = 0; i < 28; i++) { codes[i] = "P" + (i+1); scores[i] = (i % 5) + 1; }
        com.studentgui.apphelpers.Database.ensureAssessmentParts(ptId, codes);
        int sessionId = com.studentgui.apphelpers.Database.createProgressSession(studentId, ptId, LocalDate.now());
        com.studentgui.apphelpers.Database.insertAssessmentResults(sessionId, ptId, codes, scores);

        List<List<Integer>> allSkillValues = com.studentgui.apphelpers.Database.fetchLatestAssessmentResults("JUnit Smoke Student", "Braille", 5);
        assertNotNull(allSkillValues);

        JLineGraph graph = new JLineGraph();
        graph.updateWithData(allSkillValues);
    }
}
