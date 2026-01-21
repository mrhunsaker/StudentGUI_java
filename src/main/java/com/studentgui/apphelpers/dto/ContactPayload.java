package com.studentgui.apphelpers.dto;

/**
 * Typed payload for contact log entries.
 */
public class ContactPayload implements SessionPayload {
    /** Database session id. */
    public int sessionId;
    /** Guardian/parent name. */
    public String guardian;
    /** Method of contact (Phone/Email/etc). */
    public String method;
    /** Phone number. */
    public String phone;
    /** Email address. */
    public String email;
    /** Brief response summary. */
    public String response;
    /** High-level general notes. */
    public String general;
    /** Specific action items or points. */
    public String specific;
    /** Full notes text. */
    public String notes;

    /** No-arg constructor for Jackson. */
    public ContactPayload() {}

    /**
     * Create a contact payload.
     *
    * @param sessionIdParam database session id
    * @param guardianParam guardian/parent name
    * @param methodParam method of contact (Phone/Email/etc)
    * @param phoneParam phone number
    * @param emailParam email address
    * @param responseParam brief response summary
    * @param generalParam high-level general notes
    * @param specificParam specific action items or points
    * @param notesParam full notes text
     */
    public ContactPayload(final int sessionIdParam, final String guardianParam, final String methodParam, final String phoneParam, final String emailParam, final String responseParam, final String generalParam, final String specificParam, final String notesParam) {
        this.sessionId = sessionIdParam;
        this.guardian = guardianParam;
        this.method = methodParam;
        this.phone = phoneParam;
        this.email = emailParam;
        this.response = responseParam;
        this.general = generalParam;
        this.specific = specificParam;
        this.notes = notesParam;
    }

    @Override
    /**
     * getSessionId - TODO: describe this method
     * @return TODO: describe return value
     */

    public int getSessionId() { return this.sessionId; }
}
