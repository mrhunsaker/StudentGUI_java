package com.studentgui.apphelpers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class DatabaseContactLogTest {

    @Test
    public void testSaveAndFetchContactLog() throws Exception {
        SqlGenerate.initializeDatabase();
        String student = "Test Student";
        int sid = Database.getOrCreateStudent(student);
        int pt = Database.getOrCreateProgressType("ContactLog");
        int sessionId = Database.createProgressSession(sid, pt, LocalDate.now());
        Database.saveContactLog(sessionId, student, LocalDate.now().toString(), "Guardian A", "Phone", "+1234567890", "a@example.com", "Left voicemail", "General summary", "Specific item", "Detailed notes");
    com.studentgui.apphelpers.dto.ContactPayload fetched = Database.fetchLatestContactLog(student);
    assertNotNull(fetched);
    assertEquals("Guardian A", fetched.guardian);
    assertEquals("+1234567890", fetched.phone);
    assertEquals("Detailed notes", fetched.notes);
    }
}
