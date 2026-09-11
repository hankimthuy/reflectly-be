package org.mentorship.reflectly.service;

import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.dto.EmotionCountDto;
import org.mentorship.reflectly.dto.MoodSummaryDayDto;
import org.mentorship.reflectly.dto.MoodSummaryResponseDto;
import org.mentorship.reflectly.dto.UserStatsResponseDto;
import org.mentorship.reflectly.model.ConversationEntity;
import org.mentorship.reflectly.model.Emotion;
import org.mentorship.reflectly.model.EntryEntity;
import org.mentorship.reflectly.repository.ConversationRepository;
import org.mentorship.reflectly.repository.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Aggregate counters behind the profile screen: writing streak, emotion distribution, and the
 * day-bucketed mood summary.
 *
 * <p>Calendar-day bucketing needs a timezone, and no per-user timezone is stored anywhere, so
 * every entry point takes an IANA zone id supplied by the caller. An absent or unparseable zone
 * falls back to UTC rather than failing the request — a stats screen should never 500 over a
 * bad query parameter.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserStatsService {

    /** Grace period, in days, before a missed day breaks the writing streak. */
    private static final int STREAK_GRACE_DAYS = 1;

    private final EntryRepository entryRepository;
    private final ConversationRepository conversationRepository;
    private final MoodScoringService moodScoringService;

    /**
     * Profile counters. The emotion figures (distribution and most-frequent) are scoped to the
     * last {@code windowDays} days; the streak and the raw talk/entry counts are all-time.
     */
    public UserStatsResponseDto getStats(Long userId, int windowDays, String timezone) {
        ZoneId zone = resolveZone(timezone);
        String entryUserId = String.valueOf(userId);

        int streak = calculateStreak(entryRepository.findCreatedDatesByUserId(entryUserId), zone);

        LocalDate today = LocalDate.now(zone);
        Instant from = startOfDay(today.minusDays(windowDays - 1L), zone);
        Instant to = endOfDay(today, zone);
        List<EmotionCountDto> distribution = emotionDistribution(entryUserId, from, to);

        EmotionCountDto top = distribution.stream()
                .filter(row -> row.getCount() > 0)
                .findFirst()
                .orElse(null);

        return UserStatsResponseDto.builder()
                .currentStreakDays(streak)
                .mostFrequentEmotion(top == null ? null : top.getEmotion())
                .mostFrequentEmotionCount(top == null ? null : top.getCount())
                .talksCount(conversationRepository.countByUserId(userId))
                .entriesCount(entryRepository.countByUserId(entryUserId))
                .emotionsWindowDays(windowDays)
                .emotionDistribution(distribution)
                .build();
    }

    /**
     * One row per calendar day in the window (oldest first), each carrying the single heaviest
     * mood signal recorded that day — pooled across ended Coach sessions and written entries.
     * Structured facts only: any "lighter than last week" phrasing is a frontend/i18n concern.
     */
    public MoodSummaryResponseDto getMoodSummary(Long userId, int windowDays, String timezone) {
        ZoneId zone = resolveZone(timezone);
        String entryUserId = String.valueOf(userId);

        LocalDate today = LocalDate.now(zone);
        LocalDate firstDay = today.minusDays(windowDays - 1L);
        Instant from = startOfDay(firstDay, zone);
        Instant to = endOfDay(today, zone);

        Map<LocalDate, Signal> heaviestPerDay = new HashMap<>();
        collectConversationSignals(userId, from, to, zone, heaviestPerDay);
        collectEntrySignals(entryUserId, from, to, zone, heaviestPerDay);

        List<MoodSummaryDayDto> days = new ArrayList<>();
        for (LocalDate day = firstDay; !day.isAfter(today); day = day.plusDays(1)) {
            Signal signal = heaviestPerDay.get(day);
            days.add(MoodSummaryDayDto.builder()
                    .date(day)
                    .score(signal == null ? null : signal.score())
                    .emotion(signal == null ? null : signal.emotion().getValue())
                    .hasData(signal != null)
                    .build());
        }
        return MoodSummaryResponseDto.builder().days(days).build();
    }

    /**
     * Consecutive days ending today that have at least one entry, with a one-day grace period:
     * a streak survives today having no entry yet, so long as yesterday has one — but it counts
     * from yesterday, so today does not inflate it. Port of {@code calculateDayStreak} in the
     * frontend's statsUtil.ts. Entries only; Coach sessions do not feed the streak.
     */
    private int calculateStreak(List<Instant> entryTimestamps, ZoneId zone) {
        Set<LocalDate> entryDays = new HashSet<>();
        for (Instant timestamp : entryTimestamps) {
            if (timestamp != null) {
                entryDays.add(LocalDate.ofInstant(timestamp, zone));
            }
        }
        if (entryDays.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now(zone);
        LocalDate cursor;
        if (entryDays.contains(today)) {
            cursor = today;
        } else if (entryDays.contains(today.minusDays(STREAK_GRACE_DAYS))) {
            cursor = today.minusDays(STREAK_GRACE_DAYS);
        } else {
            return 0;
        }

        int streak = 0;
        while (entryDays.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    /** All 9 catalog emotions, zero-filled where unused in the window, heaviest count first. */
    private List<EmotionCountDto> emotionDistribution(String entryUserId, Instant from, Instant to) {
        Map<Emotion, Long> counts = new HashMap<>();
        for (Object[] row : entryRepository.countEmotionsByUserIdAndCreatedDateBetween(entryUserId, from, to)) {
            if (row == null || row.length < 2) {
                continue;
            }
            Emotion.fromValue((String) row[0]).ifPresent(emotion ->
                    counts.merge(emotion, ((Number) row[1]).longValue(), Long::sum));
        }

        List<EmotionCountDto> distribution = new ArrayList<>();
        for (Emotion emotion : Emotion.values()) {
            distribution.add(EmotionCountDto.builder()
                    .emotion(emotion.getValue())
                    .count(counts.getOrDefault(emotion, 0L))
                    .build());
        }
        distribution.sort(Comparator.comparingLong(EmotionCountDto::getCount).reversed());
        return distribution;
    }

    private void collectConversationSignals(Long userId, Instant from, Instant to, ZoneId zone,
                                            Map<LocalDate, Signal> heaviestPerDay) {
        List<ConversationEntity> conversations =
                conversationRepository.findByUserIdAndEndedAtBetweenOrderByEndedAtAsc(userId, from, to);
        for (ConversationEntity conversation : conversations) {
            if (conversation.getEndedAt() == null
                    || conversation.getFinalMoodEmotion() == null
                    || conversation.getFinalMoodScore() == null) {
                continue;
            }
            offer(heaviestPerDay, LocalDate.ofInstant(conversation.getEndedAt(), zone),
                    new Signal(conversation.getFinalMoodEmotion(), conversation.getFinalMoodScore(),
                            conversation.getEndedAt()));
        }
    }

    private void collectEntrySignals(String entryUserId, Instant from, Instant to, ZoneId zone,
                                     Map<LocalDate, Signal> heaviestPerDay) {
        List<EntryEntity> entries =
                entryRepository.findByUserIdAndCreatedDateBetweenOrderByCreatedDateAsc(entryUserId, from, to);
        for (EntryEntity entry : entries) {
            if (entry.getCreatedDate() == null) {
                continue;
            }
            Optional<MoodScoringService.MoodReading> reading = moodScoringService.heaviestOf(entry.getEmotions());
            if (reading.isEmpty()) {
                continue;
            }
            offer(heaviestPerDay, LocalDate.ofInstant(entry.getCreatedDate(), zone),
                    new Signal(reading.get().emotion(), reading.get().heaviness(), entry.getCreatedDate()));
        }
    }

    /** Keeps the heaviest signal for a day; ties go to the more recent one. */
    private void offer(Map<LocalDate, Signal> heaviestPerDay, LocalDate day, Signal candidate) {
        heaviestPerDay.merge(day, candidate, (existing, incoming) -> {
            int byScore = Double.compare(incoming.score(), existing.score());
            if (byScore > 0) {
                return incoming;
            }
            if (byScore < 0) {
                return existing;
            }
            return incoming.at().isAfter(existing.at()) ? incoming : existing;
        });
    }

    /** Never throws: a null, blank or unrecognised zone id degrades to UTC. */
    private ZoneId resolveZone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return ZoneOffset.UTC;
        }
        try {
            return ZoneId.of(timezone.trim());
        } catch (Exception e) {
            return ZoneOffset.UTC;
        }
    }

    private Instant startOfDay(LocalDate day, ZoneId zone) {
        return day.atStartOfDay(zone).toInstant();
    }

    private Instant endOfDay(LocalDate day, ZoneId zone) {
        return day.plusDays(1).atStartOfDay(zone).toInstant();
    }

    /** One mood data point pooled into a day: what it read, how heavy, and when. */
    private record Signal(Emotion emotion, double score, Instant at) {
        private Signal {
            Objects.requireNonNull(emotion);
            Objects.requireNonNull(at);
        }
    }
}
