package org.mentorship.reflectly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Day-bucketed mood readings for the requested window — see {@code GET /api/users/mood-summary}.
 * Every day in the window is present, oldest first, including days with no signal at all.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodSummaryResponseDto {

    private List<MoodSummaryDayDto> days;
}
