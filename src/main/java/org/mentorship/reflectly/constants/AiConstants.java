package org.mentorship.reflectly.constants;

public class AiConstants {

    /** Real-time Coach chat — quality/speed balance. */
    public static final String COACH_MODEL = "gemini-3.6-flash";

    /** Background structured-extraction pass after a session ends — cheap, no deep reasoning needed. */
    public static final String MEMORY_EXTRACTION_MODEL = "gemini-3.5-flash-lite";

    public static final int COACH_MAX_OUTPUT_TOKENS = 1024;
    public static final int EXTRACTION_MAX_OUTPUT_TOKENS = 2048;
}
