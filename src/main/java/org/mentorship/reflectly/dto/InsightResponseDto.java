package org.mentorship.reflectly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mentorship.reflectly.model.InsightCategory;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsightResponseDto {
    private String id;
    private String insightText;
    private InsightCategory category;
    private Instant createdAt;

    /** Which person (relationship map) this insight is about, if known. Null otherwise. */
    private String personId;
    private String personName;
}
