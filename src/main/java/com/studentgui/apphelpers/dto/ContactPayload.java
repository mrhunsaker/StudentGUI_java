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
     * @param sessionId database session id
     * @param guardian guardian/parent name
     * @param method method of contact (Phone/Email/etc)
     * @param phone phone number
     * @param email email address
     * @param response brief response summary
     * @param general high-level general notes
     * @param specific specific action items or points
     * @param notes full notes text
     */
    public ContactPayload(int sessionId, String guardian, String method, String phone, String email, String response, String general, String specific, String notes) {
        this.sessionId = sessionId;
        this.guardian = guardian;
        this.method = method;
        this.phone = phone;
        this.email = email;
        this.response = response;
        this.general = general;
        this.specific = specific;
        this.notes = notes;
    }

    @Override
    public int getSessionId() { return this.sessionId; }
}
