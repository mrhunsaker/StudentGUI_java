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
     * @param sessionId DB session id
     * @param program program name
     * @param topic topic name
     * @param speed words per minute
     * @param accuracy percent accuracy
     */
    public KeyboardingPayload(int sessionId, String program, String topic, int speed, int accuracy) {
        this.sessionId = sessionId;
        this.program = program;
        this.topic = topic;
        this.speed = speed;
        this.accuracy = accuracy;
    }

    @Override
    public int getSessionId() { return this.sessionId; }
}
