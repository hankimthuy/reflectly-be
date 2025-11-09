package org.mentorship.reflectly.service;

import lombok.RequiredArgsConstructor;

import org.mentorship.reflectly.dto.EntryRequestDto;
import org.mentorship.reflectly.dto.EntryResponseDto;
import org.mentorship.reflectly.exception.NotFoundException;
import org.mentorship.reflectly.exception.ValidationException;
import org.mentorship.reflectly.model.EntryEntity;
import org.mentorship.reflectly.repository.EntryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class EntryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final EntryRepository entryRepository;

    @Transactional(readOnly = true)
    public Page<EntryResponseDto> getAllEntries(String userId, int page, int pageSize) {
        Pageable pageable = createPageable(page, pageSize);
        Page<EntryEntity> entryPage = entryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return entryPage.map(this::convertToResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<EntryResponseDto> getEntriesByDateRange(String userId, String startDate, String endDate, int page, int pageSize) {
        try {
            LocalDateTime start = parseDateTime(startDate);
            LocalDateTime end = parseDateTime(endDate);
            
            // Business rule: start date must be before or equal to end date
            if (start.isAfter(end)) {
                throw new ValidationException("startDate must be before or equal to endDate");
            }
            
            Pageable pageable = createPageable(page, pageSize);
            Page<EntryEntity> entryPage = entryRepository.findByUserIdAndCreatedAtBetween(userId, start, end, pageable);
            return entryPage.map(this::convertToResponseDto);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date format. Use ISO format (yyyy-MM-ddTHH:mm:ss)");
        }
    }

    @Transactional(readOnly = true)
    public Page<EntryResponseDto> getEntriesByEmotion(String userId, String emotion, int page, int pageSize) {
        Pageable pageable = createPageable(page, pageSize);
        Page<EntryEntity> entryPage = entryRepository.findByUserIdAndEmotionsContaining(userId, emotion, pageable);
        return entryPage.map(this::convertToResponseDto);
    }

    @Transactional(readOnly = true)
    public EntryResponseDto getEntryById(String userId, String entryId) {
        EntryEntity entry = entryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new NotFoundException("Entry not found"));
        
        return convertToResponseDto(entry);
    }

    public EntryResponseDto createEntry(String userId, EntryRequestDto requestDto) {
        if (requestDto.getEmotions() == null || requestDto.getEmotions().isEmpty()) {
            throw new ValidationException("At least one emotion is required");
        }

        String entryId = UUID.randomUUID().toString();
        
        EntryEntity entry = new EntryEntity(
                entryId,
                userId,
                requestDto.getTitle(),
                requestDto.getReflection(),
                requestDto.getEmotions()
        );
        
        EntryEntity savedEntry = entryRepository.save(entry);
        return convertToResponseDto(savedEntry);
    }

    public EntryResponseDto updateEntry(String userId, String entryId, EntryRequestDto requestDto) {
        EntryEntity entry = entryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new NotFoundException("Entry not found"));

        if (requestDto.getEmotions() == null || requestDto.getEmotions().isEmpty()) {
            throw new ValidationException("At least one emotion is required");
        }

        entry.setTitle(Objects.requireNonNull(requestDto.getTitle(), "Title cannot be null"));
        entry.setReflection(Objects.requireNonNull(requestDto.getReflection(), "Reflection cannot be null"));
        entry.setEmotions(requestDto.getEmotions());

        EntryEntity savedEntry = entryRepository.save(entry);
        return convertToResponseDto(savedEntry);
    }

    public void deleteEntry(String userId, String entryId) {
        if (!entryRepository.existsByIdAndUserId(entryId, userId)) {
            throw new NotFoundException("Entry not found");
        }

        entryRepository.deleteById(entryId);
    }

    private Pageable createPageable(int page, int pageSize) {
        int validPage = Math.max(0, page);
        int validPageSize = Math.min(Math.max(1, pageSize), MAX_PAGE_SIZE);
        return PageRequest.of(validPage, validPageSize);
    }

    private EntryResponseDto convertToResponseDto(EntryEntity entity) {
        return EntryResponseDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .reflection(entity.getReflection())
                .emotions(entity.getEmotions())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private LocalDateTime parseDateTime(String dateString) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return LocalDateTime.parse(dateString, formatter);
    }
}
