package org.mentorship.reflectly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Profile-screen counters for the current user — see {@code GET /api/users/stats}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsResponseDto {

    /** Consecutive days with at least one entry, with a 1-day grace period. Entries only. */
    private int currentStreakDays;

    /** Most-tagged emotion within the same window as the distribution; null when none. */
    private String mostFrequentEmotion;

    /** Tag count of {@link #mostFrequentEmotion}; null whenever that is null. */
    private Long mostFrequentEmotionCount;

    private long talksCount;

    private long entriesCount;

    /** Window (in days) the emotion figures above were computed over. */
    private int emotionsWindowDays;

    /** All 9 catalog emotions, zero-filled, sorted by count descending. */
    private List<EmotionCountDto> emotionDistribution;
}
