package org.mentorship.reflectly.converter;

import org.mentorship.reflectly.dto.InsightResponseDto;
import org.mentorship.reflectly.model.InsightEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class InsightConverter {

    public InsightResponseDto toResponseDto(InsightEntity entity) {
        return InsightResponseDto.builder()
                .id(entity.getId())
                .insightText(entity.getInsightText())
                .category(entity.getCategory())
                .createdAt(entity.getCreatedDate())
                .personId(entity.getPerson() != null ? entity.getPerson().getId() : null)
                .personName(entity.getPerson() != null ? entity.getPerson().getName() : null)
                .build();
    }

    public Page<InsightResponseDto> toResponseDtoPage(Page<InsightEntity> entityPage) {
        return entityPage.map(this::toResponseDto);
    }
}
