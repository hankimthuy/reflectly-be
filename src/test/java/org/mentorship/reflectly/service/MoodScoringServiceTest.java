package org.mentorship.reflectly.service;

import org.junit.jupiter.api.Test;
import org.mentorship.reflectly.model.Emotion;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the local keyword mood heuristic ported from the frontend's moodUtil.ts. No Gemini
 * involved — these assertions pin the lexicon and the "heaviest match wins" rule.
 */
class MoodScoringServiceTest {

    private final MoodScoringService moodScoringService = new MoodScoringService();

    @Test
    void score_returnsEmptyForNullEmptyOrBlankText() {
        assertThat(moodScoringService.score(null)).isEmpty();
        assertThat(moodScoringService.score("")).isEmpty();
        assertThat(moodScoringService.score("   ")).isEmpty();
    }

    @Test
    void score_matchesSingleEnglishKeyword() {
        Optional<MoodScoringService.MoodReading> reading =
                moodScoringService.score("I am feeling really anxious about tomorrow");

        assertThat(reading).isPresent();
        assertThat(reading.get().emotion()).isEqualTo(Emotion.ANXIOUS);
        assertThat(reading.get().heaviness()).isEqualTo(0.95);
    }

    @Test
    void score_matchesSingleVietnameseKeyword() {
        Optional<MoodScoringService.MoodReading> reading =
                moodScoringService.score("Hôm nay mình thấy biết ơn nhiều thứ");

        assertThat(reading).isPresent();
        assertThat(reading.get().emotion()).isEqualTo(Emotion.BLESSED);
        assertThat(reading.get().heaviness()).isEqualTo(0.15);
    }

    @Test
    void score_mixedEnglishAndVietnamese_heaviestWins() {
        // "happy" (0.1) vs "lo lắng" (0.95) — the heavier read is the one worth surfacing.
        Optional<MoodScoringService.MoodReading> reading =
                moodScoringService.score("I was happy earlier nhưng giờ thì lo lắng quá");

        assertThat(reading).isPresent();
        assertThat(reading.get().emotion()).isEqualTo(Emotion.ANXIOUS);
    }

    @Test
    void score_multipleDifferentEmotions_heaviestWins() {
        // grateful (0.15), bored (0.45), angry (0.9) all match.
        Optional<MoodScoringService.MoodReading> reading =
                moodScoringService.score("grateful for the day but bored at work and angry at myself");

        assertThat(reading).isPresent();
        assertThat(reading.get().emotion()).isEqualTo(Emotion.ANGRY);
        assertThat(reading.get().heaviness()).isEqualTo(0.9);
    }

    @Test
    void score_isCaseInsensitiveIncludingVietnameseDiacritics() {
        assertThat(moodScoringService.score("TOTALLY EXHAUSTED AND DEPRESSED"))
                .get()
                .extracting(MoodScoringService.MoodReading::emotion)
                .isEqualTo(Emotion.DOWN);

        assertThat(moodScoringService.score("Mình thấy BỐI RỐI quá"))
                .get()
                .extracting(MoodScoringService.MoodReading::emotion)
                .isEqualTo(Emotion.CONFUSED);
    }

    @Test
    void score_returnsEmptyWhenNoKeywordMatches() {
        assertThat(moodScoringService.score("The quarterly report is attached to the email."))
                .isEmpty();
    }

    @Test
    void heaviestOf_picksHeaviestCatalogLabelAndIgnoresUnknownOnes() {
        assertThat(moodScoringService.heaviestOf(java.util.List.of("happy", "ANXIOUS", "not-an-emotion")))
                .get()
                .extracting(MoodScoringService.MoodReading::emotion)
                .isEqualTo(Emotion.ANXIOUS);

        assertThat(moodScoringService.heaviestOf(java.util.List.of("not-an-emotion"))).isEmpty();
        assertThat(moodScoringService.heaviestOf(java.util.List.of())).isEmpty();
        assertThat(moodScoringService.heaviestOf(null)).isEmpty();
    }
}
