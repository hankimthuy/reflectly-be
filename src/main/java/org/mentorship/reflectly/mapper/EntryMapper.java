package org.mentorship.reflectly.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mentorship.reflectly.dto.EntryRequestDto;
import org.mentorship.reflectly.dto.EntryResponseDto;
import org.mentorship.reflectly.model.EntryEntity;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EntryMapper {

    EntryResponseDto toResponseDto(EntryEntity entity);

    // Default method to create entity with id and userId using constructor (preserves entity immutability)
    default EntryEntity toEntity(EntryRequestDto requestDto, String id, String userId) {
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

    List<EntryResponseDto> toResponseDtoList(List<EntryEntity> entities);

    // MapStruct doesn't support Spring Data Page type, manual implementation required
    default Page<EntryResponseDto> toResponseDtoPage(Page<EntryEntity> entityPage) {
        if (entityPage == null) {
            return null;
        }
        return entityPage.map(this::toResponseDto);
    }

    void updateEntityFromDto(EntryRequestDto requestDto, @MappingTarget EntryEntity entity);
}

