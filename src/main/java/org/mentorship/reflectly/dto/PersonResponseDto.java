package org.mentorship.reflectly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mentorship.reflectly.model.RelationshipType;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonResponseDto {

    private String id;
    private String name;
    private RelationshipType relationshipType;
    private String notes;
    private Instant lastMentionedAt;

    /** Null if never mentioned. */
    private Long daysSinceLastMention;

    /** 0.0 (needs attention) to 1.0 (healthy) — derived from recency + recent sentiment, computed on read. */
    private double healthSignal;

    /** Short suggested nudge text, e.g. "Bạn chưa nhắc đến X gần đây — muốn ghi chú lại không?". Null if nothing to surface. */
    private String nudgeText;
}
