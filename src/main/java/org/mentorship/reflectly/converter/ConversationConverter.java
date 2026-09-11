package org.mentorship.reflectly.converter;

import org.mentorship.reflectly.dto.ConversationMessageResponseDto;
import org.mentorship.reflectly.dto.ConversationResponseDto;
import org.mentorship.reflectly.model.ConversationEntity;
import org.mentorship.reflectly.model.ConversationMessageEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConversationConverter {

    public ConversationMessageResponseDto toMessageResponseDto(ConversationMessageEntity entity) {
        return ConversationMessageResponseDto.builder()
                .id(entity.getId())
                .role(entity.getRole())
                .content(entity.getContent())
                .createdAt(entity.getCreatedDate())
                .moodEmotion(entity.getMoodEmotion())
                .moodScore(entity.getMoodScore())
                .build();
    }

    public ConversationResponseDto toResponseDto(ConversationEntity entity, List<ConversationMessageEntity> messages) {
        return ConversationResponseDto.builder()
                .id(entity.getId())
                .status(entity.getStatus())
                .startedAt(entity.getStartedAt())
                .endedAt(entity.getEndedAt())
                .messages(messages.stream().map(this::toMessageResponseDto).toList())
                .summary(entity.getSummary())
                .initialMoodEmotion(entity.getInitialMoodEmotion())
                .initialMoodScore(entity.getInitialMoodScore())
                .finalMoodEmotion(entity.getFinalMoodEmotion())
                .finalMoodScore(entity.getFinalMoodScore())
                .build();
    }
}
