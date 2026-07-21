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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class EnergyLogService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_RANGE_DAYS = 30;
    private static final int MAX_RANGE_DAYS = 366;

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

    /**
     * Return raw energy-log points for the last {@code days} days (oldest first),
     * bounded so the Dashboard can bucket them client-side in the user's local timezone.
     */
    @Transactional(readOnly = true)
    public List<EnergyLogResponseDto> getLogsInRange(String userId, Integer days) {
        int windowDays = days == null ? DEFAULT_RANGE_DAYS : Math.min(Math.max(1, days), MAX_RANGE_DAYS);
        Instant to = Instant.now();
        Instant from = to.minus(windowDays, ChronoUnit.DAYS);
        return energyLogRepository.findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(userId, from, to)
                .stream()
                .map(energyLogConverter::toResponseDto)
                .toList();
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
