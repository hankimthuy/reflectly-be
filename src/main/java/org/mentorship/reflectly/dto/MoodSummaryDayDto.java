package org.mentorship.reflectly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * One calendar day of the mood summary. Structured facts only — any "lighter than last week"
 * style wording is a frontend/i18n concern, deliberately not produced here.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodSummaryDayDto {

    /** Calendar date in the caller-supplied timezone, {@code YYYY-MM-DD}. */
    private LocalDate date;

    /** Heaviness (0..1) of the heaviest signal that day; null when {@code hasData} is false. */
    private Double score;

    /** Emotion of that heaviest signal, lowercase; null when {@code hasData} is false. */
    private String emotion;

    private boolean hasData;
}
