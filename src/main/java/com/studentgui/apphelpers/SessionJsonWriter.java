package com.studentgui.apphelpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Helper to write per-session JSON exports for app pages.
 */
public final class SessionJsonWriter {
    private static final Logger LOG = LoggerFactory.getLogger(SessionJsonWriter.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SessionJsonWriter() {}

    /**
     * Write a per-session JSON file into the student's StudentDataFiles folder.
     * The filename will include a unix timestamp to ensure uniqueness per session.
     *
     * @param student display name of the student
     * @param pageName short page identifier (e.g. "Abacus")
     * @param payload arbitrary payload object to serialize (Map or POJO)
     * @return the path to the written file, or null on failure
     */
    public static Path writeSessionJson(String student, String pageName, Object payload) {
        return writeSessionJson(student, pageName, payload, null);
    }

    /**
     * Write a per-session JSON file and optionally include an explicit sessionId.
     * If the explicit sessionId is null, this method will look for a "sessionId"
     * entry inside the payload Map and use that if present. The envelope written
     * to disk will include the sessionId when available.
     *
    * Filename format: {@code PageName-<epoch>-<readable>[-session-<sessionId>].json}
     *
     * @param student display name of the student
     * @param pageName short page identifier (e.g. "Abacus")
     * @param payload arbitrary payload object to serialize (Map or POJO)
     * @param explicitSessionId optional session id to use in the envelope and filename
     * @return the path to the written file, or null on failure
     */
    public static Path writeSessionJson(String student, String pageName, Object payload, String explicitSessionId) {
        if (student == null || student.trim().isEmpty() || pageName == null) {
            return null;
        }
        try {
            Path outDir = Helpers.APP_HOME.resolve("StudentDataFiles").resolve(Helpers.safeName(student));
            Files.createDirectories(outDir);
            long ts = Instant.now().toEpochMilli();
            // format for readability too
            String readable = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS").withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(ts));

            // Determine sessionId preference: explicit param first, then payload if it implements SessionPayload
            String sid = explicitSessionId;
            if (sid == null && payload instanceof com.studentgui.apphelpers.dto.SessionPayload) {
                int s = ((com.studentgui.apphelpers.dto.SessionPayload) payload).getSessionId();
                if (s != 0) {
                    sid = Integer.toString(s);
                }
            }

            String filename = String.format("%s-%d-%s%s.json", pageName, ts, readable, (sid != null ? "-session-" + sid : ""));
            Path outFile = outDir.resolve(filename);

            Map<String, Object> envelope = new HashMap<>();
            envelope.put("student", student);
            envelope.put("timestamp", ts);
            envelope.put("timestampIso", readable);
            envelope.put("page", pageName);
            if (sid != null) {
                envelope.put("sessionId", sid);
            }
            envelope.put("payload", payload);

            byte[] bytes = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsBytes(envelope);
            Files.write(outFile, bytes);
            LOG.info("Wrote session JSON for {} page {} to {}", student, pageName, outFile);
            return outFile;
        } catch (IOException ex) {
            LOG.warn("Unable to write session JSON for {} page {}: {}", student, pageName, ex.toString());
            return null;
        }
    }

    /**
     * Convenience overload that accepts an int sessionId to avoid callers
     * converting to String. Delegates to the string-based overload.
     *
     * @param student display name of the student
     * @param pageName short page identifier
     * @param payload arbitrary payload object
     * @param explicitSessionId numeric session id
     * @return written file path or null
     */
    public static Path writeSessionJson(String student, String pageName, Object payload, int explicitSessionId) {
        return writeSessionJson(student, pageName, payload, Integer.toString(explicitSessionId));
    }

    /**
     * Backwards-compatible convenience method for callers that still have
     * (codes,scores) arrays. It wraps them in a small Map and delegates to
     * the main payload-based writer.
     *
     * @param student the student's display name
     * @param pageName short page identifier (e.g. "Abacus")
     * @param codes array of part codes to include in the payload
     * @param scores array of scores corresponding to the codes
     * @return path to the written JSON file, or null on failure
     */
    public static Path writeSessionJson(String student, String pageName, String[] codes, int[] scores) {
        com.studentgui.apphelpers.dto.AssessmentPayload payload = new com.studentgui.apphelpers.dto.AssessmentPayload(0, codes, scores);
        return writeSessionJson(student, pageName, payload);
    }
}
