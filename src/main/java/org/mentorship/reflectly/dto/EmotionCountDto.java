package org.mentorship.reflectly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One row of the emotion distribution: a catalog emotion and how often it was tagged. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmotionCountDto {

    /** Catalog emotion, in the frontend's lowercase spelling (e.g. {@code "anxious"}). */
    private String emotion;

    private long count;
}
