package org.mentorship.reflectly.converter;

import org.mentorship.reflectly.dto.SavedFrameworkEntryResponseDto;
import org.mentorship.reflectly.model.SavedFrameworkEntryEntity;
import org.springframework.stereotype.Component;

@Component
public class SavedFrameworkEntryConverter {

    public SavedFrameworkEntryResponseDto toResponseDto(SavedFrameworkEntryEntity entity) {
        if (entity == null) {
            return null;
        }
        return SavedFrameworkEntryResponseDto.builder()
                .id(entity.getId())
                .frameworkType(entity.getFrameworkType())
                .title(entity.getTitle())
                .payload(entity.getPayload())
                .conversationId(entity.getConversation() != null ? entity.getConversation().getId() : null)
                .personId(entity.getPerson() != null ? entity.getPerson().getId() : null)
                .personName(entity.getPerson() != null ? entity.getPerson().getName() : null)
                .createdAt(entity.getCreatedDate())
                .updatedAt(entity.getLastModifiedDate())
                .build();
    }
}
