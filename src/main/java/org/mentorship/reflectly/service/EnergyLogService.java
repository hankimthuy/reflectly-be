package org.mentorship.reflectly.service;

import lombok.RequiredArgsConstructor;

import org.mentorship.reflectly.converter.EnergyLogConverter;
import org.mentorship.reflectly.dto.EnergyLogRequestDto;
import org.mentorship.reflectly.dto.EnergyLogResponseDto;
import org.mentorship.reflectly.exception.NotFoundException;
import org.mentorship.reflectly.model.EnergyLogEntity;
import org.mentorship.reflectly.repository.EnergyLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class EnergyLogService {

    private static final int MAX_PAGE_SIZE = 100;

    private final EnergyLogRepository energyLogRepository;
    private final EnergyLogConverter energyLogConverter;

    @Transactional(readOnly = true)
    public Page<EnergyLogResponseDto> getAllLogs(String userId, String contextTag, Pageable pageable) {
        Pageable validatedPageable = validateAndCreatePageable(pageable);
        Page<EnergyLogEntity> logPage = StringUtils.hasText(contextTag)
                ? energyLogRepository.findByUserIdAndContextTagOrderByLoggedAtDesc(userId, contextTag, validatedPageable)
                : energyLogRepository.findByUserIdOrderByLoggedAtDesc(userId, validatedPageable);
        return energyLogConverter.toResponseDtoPage(logPage);
    }

    public EnergyLogResponseDto createLog(String userId, EnergyLogRequestDto requestDto) {
        String logId = UUID.randomUUID().toString();
        EnergyLogEntity log = energyLogConverter.toEntity(requestDto, logId, userId);

        EnergyLogEntity savedLog = energyLogRepository.save(log);
        return energyLogConverter.toResponseDto(savedLog);
    }

    public void deleteLog(String userId, String logId) {
        if (!energyLogRepository.existsByIdAndUserId(logId, userId)) {
            throw new NotFoundException("Energy log not found");
        }

        energyLogRepository.deleteById(logId);
    }

    private Pageable validateAndCreatePageable(Pageable pageable) {
        int page = Math.max(0, pageable.getPageNumber());
        int pageSize = Math.min(Math.max(1, pageable.getPageSize()), MAX_PAGE_SIZE);
        return PageRequest.of(page, pageSize, pageable.getSort());
    }
}
