package com.studentgui.apphelpers.dto;

/**
 * Common interface for session-scoped payloads that carry a DB session id.
 */
public interface SessionPayload {
    /**
     * Return the database session id associated with this payload.
     *
     * @return the database session id for this payload (may be 0 when unknown)
     */
    int getSessionId();
}
