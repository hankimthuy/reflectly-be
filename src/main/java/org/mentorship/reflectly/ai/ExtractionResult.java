package org.mentorship.reflectly.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Shape of the structured JSON the Memory Extraction model returns for one ended conversation.
 * Purely internal — never exposed via the REST API, so no DTO validation annotations needed.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtractionResult {

    private List<ExtractedPerson> people;
    private List<ExtractedEvent> events;
    private List<ExtractedInsight> insights;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtractedPerson {
        /** Person's first name / how the user refers to them, as mentioned in the transcript. */
        private String name;
        /** One of RelationshipType enum names (FAMILY, FRIEND, PARTNER, COLLEAGUE, MANAGER, OTHER). */
        private String relationshipType;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtractedEvent {
        /** Must match a name in `people` for this same extraction. */
        private String personName;
        /** One of RelationshipEventType enum names (CONFLICT, BONDING, NEUTRAL). */
        private String eventType;
        private String summary;
        /** -1.0 (very negative) to 1.0 (very positive). */
        private Double sentimentScore;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtractedInsight {
        private String insightText;
        /** One of InsightCategory enum names (VALUE, BEHAVIOR_PATTERN, RELATIONSHIP). */
        private String category;
    }
}
