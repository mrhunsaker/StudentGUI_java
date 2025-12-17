package com.studentgui.apphelpers;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentgui.apphelpers.dto.NotesPayload;

/**
 * Unit test for SessionJsonWriter to verify envelope and filename format.
 */
public class SessionJsonWriterTest {

    @Test
    public void writeSessionJson_includesSessionIdAndPayload() throws Exception {
        String student = "UnitTestStudent-" + System.nanoTime();
        int sessionId = 314159;
        NotesPayload payload = new NotesPayload(sessionId, "unit test notes payload");

        Path out = SessionJsonWriter.writeSessionJson(student, "UnitTestPage", payload, sessionId);
        assertNotNull(out, "writeSessionJson should return a path");
        assertTrue(Files.exists(out), "written file should exist");

        String fname = out.getFileName().toString();
        assertTrue(fname.contains("UnitTestPage"), "filename should contain page name");
        assertTrue(fname.contains("-session-" + sessionId), "filename should include session id segment");

        byte[] data = Files.readAllBytes(out);
        ObjectMapper m = new ObjectMapper();
        JsonNode root = m.readTree(data);
        assertEquals(student, root.get("student").asText());
        assertEquals("UnitTestPage", root.get("page").asText());
        assertTrue(root.has("sessionId"));
        assertEquals(sessionId, root.get("sessionId").asInt());

        JsonNode payloadNode = root.get("payload");
        assertNotNull(payloadNode);
        assertEquals("unit test notes payload", payloadNode.get("notes").asText());

        // cleanup - Files.deleteIfExists throws IOException; catch that specifically
        try { Files.deleteIfExists(out); } catch (java.io.IOException ex) { /* best-effort cleanup */ }
    }
}
