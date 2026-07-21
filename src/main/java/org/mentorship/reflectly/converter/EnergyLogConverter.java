package org.mentorship.reflectly.converter;

import org.mentorship.reflectly.dto.EnergyLogRequestDto;
import org.mentorship.reflectly.dto.EnergyLogResponseDto;
import org.mentorship.reflectly.model.EnergyLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class EnergyLogConverter {

    public EnergyLogResponseDto toResponseDto(EnergyLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return EnergyLogResponseDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .level(entity.getLevel())
                .contextTag(entity.getContextTag())
                .notes(entity.getNotes())
                .loggedAt(entity.getLoggedAt())
                .createdAt(entity.getCreatedDate())
                .build();
    }

    public EnergyLogEntity toEntity(EnergyLogRequestDto requestDto, String id, String userId) {
        if (requestDto == null) {
            return null;
        }
        return new EnergyLogEntity(
                id,
                userId,
                requestDto.getLevel(),
                requestDto.getContextTag(),
                requestDto.getNotes(),
                requestDto.getLoggedAt()
        );
    }

    public Page<EnergyLogResponseDto> toResponseDtoPage(Page<EnergyLogEntity> entityPage) {
        if (entityPage == null) {
            return null;
        }
        return entityPage.map(this::toResponseDto);
    }
}
