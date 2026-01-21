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
    * @param sessionIdParam DB session id
    * @param notesParam freeform notes
     */
    public NotesPayload(final int sessionIdParam, final String notesParam) {
        this.sessionId = sessionIdParam;
        this.notes = notesParam;
    }

    @Override
    /**
     * Return the database session id associated with this notes payload.
     *
     * @return numeric session id for the recorded notes entry
     */

    public int getSessionId() { return this.sessionId; }
}
