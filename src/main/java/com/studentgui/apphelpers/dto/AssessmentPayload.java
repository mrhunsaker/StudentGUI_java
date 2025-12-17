package com.studentgui.apphelpers.dto;

import java.util.Arrays;

/**
 * Typed payload for assessment-style pages (codes + scores).
 */
public class AssessmentPayload implements SessionPayload {
    /** Database session id for this payload. */
    public int sessionId;
    /** Array of part codes (e.g. "P1_1"). */
    public String[] codes;
    /** Parallel array of integer scores. */
    public int[] scores;

    /** No-arg constructor for Jackson and tests. */
    public AssessmentPayload() {}

    /**
     * Create an assessment payload.
     *
    * @param sessionIdParam numeric DB session id
    * @param codesParam array of part codes
    * @param scoresParam array of scores
     */
    public AssessmentPayload(final int sessionIdParam, final String[] codesParam, final int[] scoresParam) {
        this.sessionId = sessionIdParam;
        this.codes = codesParam;
        this.scores = scoresParam;
    }

    @Override
    public int getSessionId() {
        return this.sessionId;
    }

    @Override
    public String toString() {
        return "AssessmentPayload{sessionId=" + sessionId + ", codes=" + Arrays.toString(codes) + ", scores=" + Arrays.toString(scores) + "}";
    }
}
