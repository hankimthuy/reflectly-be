package org.mentorship.reflectly.converter;

import org.mentorship.reflectly.dto.EntryRequestDto;
import org.mentorship.reflectly.dto.EntryResponseDto;
import org.mentorship.reflectly.model.EntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntryConverter {

    public EntryResponseDto toResponseDto(EntryEntity entity) {
        if (entity == null) {
            return null;
        }
        return EntryResponseDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .reflection(entity.getReflection())
                .emotions(entity.getEmotions())
                .createdAt(entity.getCreatedDate())
                .updatedAt(entity.getLastModifiedDate())
                .build();
    }

    public EntryEntity toEntity(EntryRequestDto requestDto, String id, String userId) {
        if (requestDto == null) {
            return null;
        }
        return new EntryEntity(
                id,
                userId,
                requestDto.getTitle(),
                requestDto.getReflection(),
                requestDto.getEmotions()
        );
    }

    public List<EntryResponseDto> toResponseDtoList(List<EntryEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public Page<EntryResponseDto> toResponseDtoPage(Page<EntryEntity> entityPage) {
        if (entityPage == null) {
            return null;
        }
        return entityPage.map(this::toResponseDto);
    }

    public void updateEntityFromDto(EntryRequestDto requestDto, EntryEntity entity) {
        if (requestDto == null || entity == null) {
            return;
        }
        entity.setTitle(requestDto.getTitle());
        entity.setReflection(requestDto.getReflection());
        entity.setEmotions(requestDto.getEmotions());
    }
}

