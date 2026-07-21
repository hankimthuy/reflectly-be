package org.mentorship.reflectly.converter;

import org.mentorship.reflectly.dto.ActionProtocolRequestDto;
import org.mentorship.reflectly.dto.ActionProtocolResponseDto;
import org.mentorship.reflectly.dto.ProtocolUsageResponseDto;
import org.mentorship.reflectly.model.ActionProtocolEntity;
import org.mentorship.reflectly.model.ProtocolUsageEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ActionProtocolConverter {

    public ActionProtocolResponseDto toResponseDto(ActionProtocolEntity entity) {
        if (entity == null) {
            return null;
        }
        return ActionProtocolResponseDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .trigger(entity.getTrigger())
                .script(entity.getScript())
                .usageCount(entity.getUsageCount())
                .lastUsedAt(entity.getLastUsedAt())
                .createdAt(entity.getCreatedDate())
                .updatedAt(entity.getLastModifiedDate())
                .build();
    }

    public ActionProtocolEntity toEntity(ActionProtocolRequestDto requestDto, String id, String userId) {
        if (requestDto == null) {
            return null;
        }
        return new ActionProtocolEntity(
                id,
                userId,
                requestDto.getTitle(),
                requestDto.getTrigger(),
                requestDto.getScript()
        );
    }

    public void updateEntityFromDto(ActionProtocolRequestDto requestDto, ActionProtocolEntity entity) {
        if (requestDto == null || entity == null) {
            return;
        }
        entity.setTitle(requestDto.getTitle());
        entity.setTrigger(requestDto.getTrigger());
        entity.setScript(requestDto.getScript());
    }

    public Page<ActionProtocolResponseDto> toResponseDtoPage(Page<ActionProtocolEntity> entityPage) {
        if (entityPage == null) {
            return null;
        }
        return entityPage.map(this::toResponseDto);
    }

    public ProtocolUsageResponseDto toUsageResponseDto(ProtocolUsageEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProtocolUsageResponseDto.builder()
                .id(entity.getId())
                .protocolId(entity.getProtocolId())
                .effectiveness(entity.getEffectiveness())
                .note(entity.getNote())
                .usedAt(entity.getUsedAt())
                .build();
    }
}
