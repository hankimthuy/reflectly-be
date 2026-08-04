package org.mentorship.reflectly.service;

import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.converter.InsightConverter;
import org.mentorship.reflectly.dto.InsightResponseDto;
import org.mentorship.reflectly.model.InsightEntity;
import org.mentorship.reflectly.repository.InsightRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InsightService {

    private static final int MAX_PAGE_SIZE = 100;

    private final InsightRepository insightRepository;
    private final InsightConverter insightConverter;

    public Page<InsightResponseDto> getAllInsights(Long userId, Pageable pageable) {
        int page = Math.max(0, pageable.getPageNumber());
        int pageSize = Math.min(Math.max(1, pageable.getPageSize()), MAX_PAGE_SIZE);
        Pageable validated = PageRequest.of(page, pageSize, pageable.getSort());

        Page<InsightEntity> insightPage = insightRepository.findByUserIdOrderByCreatedDateDesc(userId, validated);
        return insightConverter.toResponseDtoPage(insightPage);
    }
}
