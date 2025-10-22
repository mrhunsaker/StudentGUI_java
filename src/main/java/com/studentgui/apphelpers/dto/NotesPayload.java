package com.studentgui.apphelpers.dto;

/**
 * Typed payload for freeform notes pages.
 */
public class NotesPayload implements SessionPayload {
    /** Database session id. */
    public int sessionId;
    /** The freeform notes text. */
    public String notes;

    /** No-arg constructor for Jackson. */
    public NotesPayload() {}

    /**
     * Create a notes payload.
     *
     * @param sessionId DB session id
     * @param notes freeform notes
     */
    public NotesPayload(int sessionId, String notes) {
        this.sessionId = sessionId;
        this.notes = notes;
    }

    @Override
    public int getSessionId() { return this.sessionId; }
}
