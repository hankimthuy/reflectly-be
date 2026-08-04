package org.mentorship.reflectly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mentorship.reflectly.model.ConversationStatus;

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
}
