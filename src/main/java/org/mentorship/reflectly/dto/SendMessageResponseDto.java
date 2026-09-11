package org.mentorship.reflectly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response of {@code POST /api/conversations/{id}/messages}: both the persisted user message
 * (carrying its mood reading, which only the server can compute) and the Coach's reply.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendMessageResponseDto {

    private ConversationMessageResponseDto userMessage;

    private ConversationMessageResponseDto assistantMessage;
}
