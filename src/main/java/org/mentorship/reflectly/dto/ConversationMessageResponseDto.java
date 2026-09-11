package org.mentorship.reflectly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mentorship.reflectly.model.Emotion;
import org.mentorship.reflectly.model.MessageRole;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMessageResponseDto {
    private String id;
    private MessageRole role;
    private String content;
    private Instant createdAt;

    /** Keyword-heuristic mood read off this message; null when nothing matched. */
    private Emotion moodEmotion;

    /** Heaviness (0..1) of {@link #moodEmotion}; null whenever that is null. */
    private Double moodScore;
}
