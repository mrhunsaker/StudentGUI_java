package com.studentgui.apphelpers.dto;

/**
 * Typed payload for Keyboarding page.
 */
public class KeyboardingPayload implements SessionPayload {
    /** Database session id. */
    public int sessionId;
    /** Program or curriculum name. */
    public String program;
    /** Topic or lesson name. */
    public String topic;
    /** Speed in WPM. */
    public int speed;
    /** Accuracy percentage. */
    public int accuracy;

    /** No-arg constructor for Jackson. */
    public KeyboardingPayload() {}

    /**
     * Create keyboarding payload.
     *
    * @param sessionIdParam DB session id
    * @param programParam program name
    * @param topicParam topic name
    * @param speedParam words per minute
    * @param accuracyParam percent accuracy
     */
    public KeyboardingPayload(final int sessionIdParam, final String programParam, final String topicParam, final int speedParam, final int accuracyParam) {
        this.sessionId = sessionIdParam;
        this.program = programParam;
        this.topic = topicParam;
        this.speed = speedParam;
        this.accuracy = accuracyParam;
    }

    @Override
    /**
     * Return the database session id associated with this keyboarding payload.
     *
     * @return numeric session id for the recorded keyboarding session
     */

    public int getSessionId() { return this.sessionId; }
}
