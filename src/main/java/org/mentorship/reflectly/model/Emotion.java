package org.mentorship.reflectly.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;
import java.util.Optional;

/**
 * The 9-emotion catalog shared with the frontend ({@code reflectly-fe/src/models/emotion.ts}).
 *
 * <p>Persisted by constant name ({@code "ANXIOUS"}) like every other enum in this codebase, but
 * serialized to JSON in the frontend's lowercase spelling ({@code "anxious"}) so API consumers
 * can use the value directly as a key into their emotion catalog — the same lowercase spelling
 * already stored in {@code entry_emotions} and returned by the entries API.
 */
public enum Emotion {

    HAPPY("happy"),
    BLESSED("blessed"),
    GOOD("good"),
    CONFUSED("confused"),
    BORED("bored"),
    AWKWARD("awkward"),
    ANGRY("angry"),
    ANXIOUS("anxious"),
    DOWN("down");

    private final String value;

    Emotion(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Case-insensitive lookup accepting either spelling — the lowercase wire/entry value
     * ({@code "anxious"}) or the constant name ({@code "ANXIOUS"}). Never throws; unknown or
     * null input yields an empty Optional, since user-supplied emotion strings on entries are
     * not constrained to this catalog.
     */
    public static Optional<Emotion> fromValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        for (Emotion emotion : values()) {
            if (emotion.value.equals(normalized)) {
                return Optional.of(emotion);
            }
        }
        return Optional.empty();
    }
}
