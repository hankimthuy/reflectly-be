package org.mentorship.reflectly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mentorship.reflectly.model.ConversationStatus;
import org.mentorship.reflectly.model.Emotion;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponseDto {
    private String id;
    private ConversationStatus status;
    private Instant startedAt;
    private Instant endedAt;
    private List<ConversationMessageResponseDto> messages;

    /** AI-generated markdown recap, null until requested via POST /{id}/summarize. */
    private String summary;

    /**
     * Mood arc of the session — first and last mood-scored USER message, computed when the
     * session ends. All four are null while the session is still ACTIVE, and stay null if no
     * message ever matched the mood heuristic.
     */
    private Emotion initialMoodEmotion;
    private Double initialMoodScore;
    private Emotion finalMoodEmotion;
    private Double finalMoodScore;
}
