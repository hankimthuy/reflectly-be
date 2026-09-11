package org.mentorship.reflectly.service;

import org.mentorship.reflectly.model.Emotion;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Reads a coarse mood signal off free text using a small EN+VI keyword lexicon.
 *
 * <p><b>This is a local heuristic — it does not call Gemini or any other model.</b> It is the
 * server-side port of {@code reflectly-fe/src/utils/moodUtil.ts}, moved here so a reading can be
 * persisted with the message that produced it instead of being recomputed in the browser. Treat
 * any single reading as illustrative, not as sentiment analysis, and never as a diagnosis.
 *
 * <p>Stateless and side-effect free.
 */
@Component
public class MoodScoringService {

    /**
     * Where each emotion sits on the "heavy → light" scale the Aura Soft redesign reads moods on.
     * Kept byte-for-byte in step with {@code EMOTION_HEAVINESS} in the frontend's moodUtil.ts so
     * the same emotion weighs the same on both sides of the API.
     */
    private static final Map<Emotion, Double> HEAVINESS = Map.of(
            Emotion.ANXIOUS, 0.95,
            Emotion.ANGRY, 0.9,
            Emotion.DOWN, 0.85,
            Emotion.AWKWARD, 0.6,
            Emotion.CONFUSED, 0.55,
            Emotion.BORED, 0.45,
            Emotion.GOOD, 0.25,
            Emotion.BLESSED, 0.15,
            Emotion.HAPPY, 0.1
    );

    /** Mirrors {@code MOOD_KEYWORDS} in the frontend's moodUtil.ts. */
    private static final Map<Emotion, List<String>> KEYWORDS = Map.of(
            Emotion.ANXIOUS, List.of("anxious", "anxiety", "nervous", "worried", "scared", "lo lắng", "lo âu", "sợ"),
            Emotion.ANGRY, List.of("angry", "furious", "mad", "pissed", "giận", "tức", "bực"),
            Emotion.DOWN, List.of("sad", "down", "depressed", "hopeless", "buồn", "chán nản", "tuyệt vọng"),
            Emotion.AWKWARD, List.of("awkward", "embarrassed", "uncomfortable", "ngượng", "xấu hổ"),
            Emotion.CONFUSED, List.of("confused", "unsure", "lost", "bối rối", "không chắc"),
            Emotion.BORED, List.of("bored", "nothing happened", "chán", "buồn chán"),
            Emotion.GOOD, List.of("good", "fine", "okay", "ổn", "tốt"),
            Emotion.BLESSED, List.of("grateful", "thankful", "blessed", "biết ơn", "may mắn"),
            Emotion.HAPPY, List.of("happy", "glad", "excited", "vui", "hạnh phúc")
    );

    /** A mood reading: the emotion matched and where it sits on the 0..1 heaviness scale. */
    public record MoodReading(Emotion emotion, double heaviness) {
    }

    /**
     * The heaviest emotion whose keywords appear anywhere in {@code text}, or empty when none do
     * — which is the common case for ordinary sentences, by design.
     */
    public Optional<MoodReading> score(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        String lower = text.toLowerCase(Locale.ROOT);
        return KEYWORDS.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(lower::contains))
                .map(entry -> toReading(entry.getKey()))
                .max(Comparator.comparingDouble(MoodReading::heaviness));
    }

    /**
     * The heaviest of a set of already-known emotion labels (e.g. the tags on an entry), matched
     * against the catalog case-insensitively. Labels outside the catalog are ignored.
     */
    public Optional<MoodReading> heaviestOf(Collection<String> emotionLabels) {
        if (emotionLabels == null || emotionLabels.isEmpty()) {
            return Optional.empty();
        }
        return emotionLabels.stream()
                .map(Emotion::fromValue)
                .flatMap(Optional::stream)
                .map(this::toReading)
                .max(Comparator.comparingDouble(MoodReading::heaviness));
    }

    /** Where an emotion sits on the 0..1 heaviness scale. */
    public double heaviness(Emotion emotion) {
        return HEAVINESS.getOrDefault(emotion, 0.0);
    }

    private MoodReading toReading(Emotion emotion) {
        return new MoodReading(emotion, heaviness(emotion));
    }
}
