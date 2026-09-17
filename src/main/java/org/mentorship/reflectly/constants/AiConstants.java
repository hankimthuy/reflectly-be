package org.mentorship.reflectly.constants;

public class AiConstants {

    /**
     * Real-time Coach chat — quality/speed balance. An OpenRouter model slug; the leading "~"
     * pins the provider's own "latest" alias rather than a frozen snapshot.
     */
    public static final String COACH_MODEL = "~deepseek/deepseek-flash-latest";

    /** Background structured-extraction pass after a session ends — cheap, no deep reasoning needed. */
    public static final String MEMORY_EXTRACTION_MODEL = "~deepseek/deepseek-flash-latest";

    public static final int COACH_MAX_OUTPUT_TOKENS = 1024;
    public static final int EXTRACTION_MAX_OUTPUT_TOKENS = 2048;
}
