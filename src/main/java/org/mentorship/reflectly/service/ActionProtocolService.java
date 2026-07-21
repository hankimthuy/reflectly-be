package org.mentorship.reflectly.service;

import lombok.RequiredArgsConstructor;

import org.mentorship.reflectly.converter.ActionProtocolConverter;
import org.mentorship.reflectly.dto.ActionProtocolRequestDto;
import org.mentorship.reflectly.dto.ActionProtocolResponseDto;
import org.mentorship.reflectly.dto.MarkProtocolUsedRequestDto;
import org.mentorship.reflectly.exception.NotFoundException;
import org.mentorship.reflectly.model.ActionProtocolEntity;
import org.mentorship.reflectly.model.ProtocolUsageEntity;
import org.mentorship.reflectly.repository.ActionProtocolRepository;
import org.mentorship.reflectly.repository.ProtocolUsageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class ActionProtocolService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ActionProtocolRepository actionProtocolRepository;
    private final ProtocolUsageRepository protocolUsageRepository;
    private final ActionProtocolConverter actionProtocolConverter;

    @Transactional(readOnly = true)
    public Page<ActionProtocolResponseDto> getAllProtocols(String userId, Pageable pageable) {
        Pageable validatedPageable = validateAndCreatePageable(pageable);
        Page<ActionProtocolEntity> protocolPage = actionProtocolRepository.findByUserIdOrderByCreatedDateDesc(userId, validatedPageable);
        return actionProtocolConverter.toResponseDtoPage(protocolPage);
    }

    @Transactional(readOnly = true)
    public ActionProtocolResponseDto getProtocolById(String userId, String protocolId) {
        ActionProtocolEntity protocol = actionProtocolRepository.findByIdAndUserId(protocolId, userId)
                .orElseThrow(() -> new NotFoundException("Action protocol not found"));
        return actionProtocolConverter.toResponseDto(protocol);
    }

    public ActionProtocolResponseDto createProtocol(String userId, ActionProtocolRequestDto requestDto) {
        String protocolId = UUID.randomUUID().toString();
        ActionProtocolEntity protocol = actionProtocolConverter.toEntity(requestDto, protocolId, userId);

        ActionProtocolEntity savedProtocol = actionProtocolRepository.save(protocol);
        return actionProtocolConverter.toResponseDto(savedProtocol);
    }

    public ActionProtocolResponseDto updateProtocol(String userId, String protocolId, ActionProtocolRequestDto requestDto) {
        ActionProtocolEntity protocol = actionProtocolRepository.findByIdAndUserId(protocolId, userId)
                .orElseThrow(() -> new NotFoundException("Action protocol not found"));

        actionProtocolConverter.updateEntityFromDto(requestDto, protocol);

        ActionProtocolEntity savedProtocol = actionProtocolRepository.save(protocol);
        return actionProtocolConverter.toResponseDto(savedProtocol);
    }

    public void deleteProtocol(String userId, String protocolId) {
        if (!actionProtocolRepository.existsByIdAndUserId(protocolId, userId)) {
            throw new NotFoundException("Action protocol not found");
        }

        actionProtocolRepository.deleteById(protocolId);
    }

    /**
     * Marks a protocol as used: increments its usage counter, stamps lastUsedAt, and
     * appends an entry to the usage log so effectiveness can be tracked over time.
     */
    public ActionProtocolResponseDto markUsed(String userId, String protocolId, MarkProtocolUsedRequestDto requestDto) {
        ActionProtocolEntity protocol = actionProtocolRepository.findByIdAndUserId(protocolId, userId)
                .orElseThrow(() -> new NotFoundException("Action protocol not found"));

        Instant usedAt = Instant.now();
        protocol.recordUsage(usedAt);
        ActionProtocolEntity savedProtocol = actionProtocolRepository.save(protocol);

        ProtocolUsageEntity usage = new ProtocolUsageEntity(
                UUID.randomUUID().toString(),
                protocolId,
                userId,
                requestDto.getEffectiveness(),
                requestDto.getNote(),
                usedAt
        );
        protocolUsageRepository.save(usage);

        return actionProtocolConverter.toResponseDto(savedProtocol);
    }

    private Pageable validateAndCreatePageable(Pageable pageable) {
        int page = Math.max(0, pageable.getPageNumber());
        int pageSize = Math.min(Math.max(1, pageable.getPageSize()), MAX_PAGE_SIZE);
        return PageRequest.of(page, pageSize, pageable.getSort());
    }
}
