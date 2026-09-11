package org.mentorship.reflectly.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mentorship.reflectly.dto.EmotionCountDto;
import org.mentorship.reflectly.dto.MoodSummaryDayDto;
import org.mentorship.reflectly.dto.MoodSummaryResponseDto;
import org.mentorship.reflectly.dto.UserStatsResponseDto;
import org.mentorship.reflectly.model.ConversationEntity;
import org.mentorship.reflectly.model.Emotion;
import org.mentorship.reflectly.model.EntryEntity;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.repository.ConversationRepository;
import org.mentorship.reflectly.repository.EntryRepository;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Covers the profile-stats aggregation: the entries-only writing streak (with its 1-day grace
 * period), the windowed emotion distribution, and the day-bucketed mood summary.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserStatsServiceTest {

    private static final Long USER_ID = 42L;
    private static final String ENTRY_USER_ID = "42";

    @Mock
    private EntryRepository entryRepository;
    @Mock
    private ConversationRepository conversationRepository;

    private UserStatsService userStatsService;

    @BeforeEach
    void setUp() {
        userStatsService = new UserStatsService(entryRepository, conversationRepository, new MoodScoringService());
        stubEmpty();
    }

    private void stubEmpty() {
        when(entryRepository.findCreatedDatesByUserId(anyString())).thenReturn(List.of());
        when(entryRepository.countEmotionsByUserIdAndCreatedDateBetween(anyString(), any(), any()))
                .thenReturn(List.of());
        when(entryRepository.findByUserIdAndCreatedDateBetweenOrderByCreatedDateAsc(anyString(), any(), any()))
                .thenReturn(List.of());
        when(entryRepository.countByUserId(anyString())).thenReturn(0L);
        when(conversationRepository.countByUserId(any())).thenReturn(0L);
        when(conversationRepository.findByUserIdAndEndedAtBetweenOrderByEndedAtAsc(any(), any(), any()))
                .thenReturn(List.of());
    }

    // ---------- streak ----------

    @Test
    void streak_isZeroWithNoEntries() {
        assertThat(stats().getCurrentStreakDays()).isZero();
    }

    @Test
    void streak_isOneForASingleEntryToday() {
        stubEntryDates(daysAgo(0));

        assertThat(stats().getCurrentStreakDays()).isEqualTo(1);
    }

    @Test
    void streak_survivesTodayBeingEmptyWhenYesterdayHasAnEntry() {
        stubEntryDates(daysAgo(1));

        // Grace period: the streak still stands, but counts from yesterday — today does not inflate it.
        assertThat(stats().getCurrentStreakDays()).isEqualTo(1);
    }

    @Test
    void streak_isZeroWhenTheMostRecentEntryIsTwoDaysAgo() {
        stubEntryDates(daysAgo(2));

        assertThat(stats().getCurrentStreakDays()).isZero();
    }

    @Test
    void streak_countsSeveralEntriesOnTheSameCalendarDayOnce() {
        stubEntryDates(daysAgo(0), daysAgo(0).plus(3, ChronoUnit.HOURS), daysAgo(1), daysAgo(2));

        assertThat(stats().getCurrentStreakDays()).isEqualTo(3);
    }

    @Test
    void streak_fallsBackToUtcOnAnUnparseableTimezoneInsteadOfThrowing() {
        stubEntryDates(daysAgo(0));

        UserStatsResponseDto withGarbage = userStatsService.getStats(USER_ID, 90, "Not/A_Zone");
        UserStatsResponseDto withUtc = userStatsService.getStats(USER_ID, 90, "UTC");
        UserStatsResponseDto withNull = userStatsService.getStats(USER_ID, 90, null);

        assertThat(withGarbage.getCurrentStreakDays()).isEqualTo(withUtc.getCurrentStreakDays());
        assertThat(withNull.getCurrentStreakDays()).isEqualTo(withUtc.getCurrentStreakDays());
    }

    // ---------- emotion distribution ----------

    @Test
    void distribution_listsAllNineCatalogEmotionsEvenAtZeroCount() {
        List<EmotionCountDto> distribution = stats().getEmotionDistribution();

        assertThat(distribution).hasSize(9);
        assertThat(distribution).extracting(EmotionCountDto::getEmotion)
                .containsExactlyInAnyOrder("happy", "blessed", "good", "confused", "bored",
                        "awkward", "angry", "anxious", "down");
        assertThat(distribution).allMatch(row -> row.getCount() == 0);
    }

    @Test
    void distribution_excludesEntriesOutsideTheWindowAndRanksHeaviestCountFirst() {
        // The repository query itself applies the window; this asserts the service passes a window
        // bounded by `days` and ranks what comes back, rather than re-filtering in memory.
        when(entryRepository.countEmotionsByUserIdAndCreatedDateBetween(eq(ENTRY_USER_ID), any(), any()))
                .thenReturn(List.of(
                        new Object[]{"happy", 2L},
                        new Object[]{"anxious", 5L},
                        new Object[]{"not-a-catalog-emotion", 99L}));

        UserStatsResponseDto stats = userStatsService.getStats(USER_ID, 30, "UTC");

        assertThat(stats.getEmotionsWindowDays()).isEqualTo(30);
        assertThat(stats.getEmotionDistribution()).hasSize(9);
        assertThat(stats.getEmotionDistribution().get(0).getEmotion()).isEqualTo("anxious");
        assertThat(stats.getEmotionDistribution().get(0).getCount()).isEqualTo(5);
        assertThat(stats.getEmotionDistribution().get(1).getEmotion()).isEqualTo("happy");
        assertThat(stats.getMostFrequentEmotion()).isEqualTo("anxious");
        assertThat(stats.getMostFrequentEmotionCount()).isEqualTo(5L);
    }

    @Test
    void mostFrequentEmotion_isNullWhenNothingInWindowHasEmotions() {
        UserStatsResponseDto stats = stats();

        assertThat(stats.getMostFrequentEmotion()).isNull();
        assertThat(stats.getMostFrequentEmotionCount()).isNull();
    }

    @Test
    void stats_reportTalkAndEntryTotals() {
        when(conversationRepository.countByUserId(USER_ID)).thenReturn(7L);
        when(entryRepository.countByUserId(ENTRY_USER_ID)).thenReturn(13L);

        UserStatsResponseDto stats = stats();

        assertThat(stats.getTalksCount()).isEqualTo(7L);
        assertThat(stats.getEntriesCount()).isEqualTo(13L);
    }

    // ---------- mood summary ----------

    @Test
    void moodSummary_picksTheSingleHeaviestSignalOnADayHoldingBothAConversationAndEntries() {
        Instant today = daysAgo(0);
        when(conversationRepository.findByUserIdAndEndedAtBetweenOrderByEndedAtAsc(eq(USER_ID), any(), any()))
                .thenReturn(List.of(endedConversation(today, Emotion.GOOD, 0.25)));
        when(entryRepository.findByUserIdAndCreatedDateBetweenOrderByCreatedDateAsc(eq(ENTRY_USER_ID), any(), any()))
                .thenReturn(List.of(entryWithEmotions(today, List.of("happy", "angry"))));

        MoodSummaryDayDto todayRow = lastDay(userStatsService.getMoodSummary(USER_ID, 7, "UTC"));

        // angry (0.9) outweighs both the session's final read (0.25) and happy (0.1).
        assertThat(todayRow.isHasData()).isTrue();
        assertThat(todayRow.getEmotion()).isEqualTo("angry");
        assertThat(todayRow.getScore()).isEqualTo(0.9);
    }

    @Test
    void moodSummary_marksDaysWithoutAnySignalAsNoData() {
        MoodSummaryResponseDto summary = userStatsService.getMoodSummary(USER_ID, 7, "UTC");

        assertThat(summary.getDays()).hasSize(7);
        assertThat(summary.getDays()).allSatisfy(day -> {
            assertThat(day.isHasData()).isFalse();
            assertThat(day.getScore()).isNull();
            assertThat(day.getEmotion()).isNull();
        });
        // Oldest first, ending today.
        assertThat(summary.getDays().get(6).getDate()).isEqualTo(LocalDate.now(ZoneOffset.UTC));
    }

    @Test
    void moodSummary_usesTheConversationSignalWhenNoEntryIsHeavier() {
        when(conversationRepository.findByUserIdAndEndedAtBetweenOrderByEndedAtAsc(eq(USER_ID), any(), any()))
                .thenReturn(List.of(endedConversation(daysAgo(0), Emotion.ANXIOUS, 0.95)));

        MoodSummaryDayDto todayRow = lastDay(userStatsService.getMoodSummary(USER_ID, 7, "UTC"));

        assertThat(todayRow.isHasData()).isTrue();
        assertThat(todayRow.getEmotion()).isEqualTo("anxious");
    }

    // ---------- helpers ----------

    private UserStatsResponseDto stats() {
        return userStatsService.getStats(USER_ID, 90, "UTC");
    }

    private MoodSummaryDayDto lastDay(MoodSummaryResponseDto summary) {
        return summary.getDays().get(summary.getDays().size() - 1);
    }

    private void stubEntryDates(Instant... timestamps) {
        when(entryRepository.findCreatedDatesByUserId(ENTRY_USER_ID)).thenReturn(List.of(timestamps));
    }

    /** Midday UTC on the day N days before today, so tests never straddle a day boundary. */
    private Instant daysAgo(int days) {
        return LocalDate.now(ZoneOffset.UTC).minusDays(days).atTime(12, 0).toInstant(ZoneOffset.UTC);
    }

    private ConversationEntity endedConversation(Instant endedAt, Emotion emotion, double score) {
        UserEntity user = new UserEntity();
        user.setId(USER_ID);
        ConversationEntity conversation = new ConversationEntity("conversation-" + endedAt, user);
        conversation.end();
        conversation.setEndedAt(endedAt);
        conversation.setFinalMoodEmotion(emotion);
        conversation.setFinalMoodScore(score);
        return conversation;
    }

    private EntryEntity entryWithEmotions(Instant createdDate, List<String> emotions) {
        EntryEntity entry = new EntryEntity("entry-" + createdDate, ENTRY_USER_ID, "t", "r", emotions);
        // createdDate is managed by JPA auditing, so it has to be set reflectively in a plain unit test.
        ReflectionTestUtils.setField(entry, "createdDate", createdDate);
        return entry;
    }
}
