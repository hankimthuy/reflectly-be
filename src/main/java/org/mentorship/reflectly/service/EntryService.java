package org.mentorship.reflectly.service;

import lombok.RequiredArgsConstructor;

import org.mentorship.reflectly.mapper.EntryMapper;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class EntryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final EntryRepository entryRepository;
    private final EntryMapper entryMapper;

    @Transactional(readOnly = true)
    public Page<EntryResponseDto> getAllEntries(String userId, Pageable pageable) {
        Pageable validatedPageable = validatePageable(pageable);
        Page<EntryEntity> entryPage = entryRepository.findByUserIdOrderByCreatedAtDesc(userId, validatedPageable);
        return entryMapper.toResponseDtoPage(entryPage);
    }

    @Transactional(readOnly = true)
    public Page<EntryResponseDto> getEntriesByDateRange(String userId, String startDate, String endDate, Pageable pageable) {
        try {
            LocalDateTime start = parseDateTime(startDate);
            LocalDateTime end = parseDateTime(endDate);
            
            // Business rule: start date must be before or equal to end date
            if (start.isAfter(end)) {
                throw new ValidationException("startDate must be before or equal to endDate");
            }
            
            Pageable validatedPageable = validatePageable(pageable);
            Page<EntryEntity> entryPage = entryRepository.findByUserIdAndCreatedAtBetween(userId, start, end, validatedPageable);
            return entryMapper.toResponseDtoPage(entryPage);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date format. Use ISO format (yyyy-MM-ddTHH:mm:ss)");
        }
    }

    @Transactional(readOnly = true)
    public Page<EntryResponseDto> getEntriesByEmotion(String userId, String emotion, Pageable pageable) {
        Pageable validatedPageable = validatePageable(pageable);
        Page<EntryEntity> entryPage = entryRepository.findByUserIdAndEmotionsContaining(userId, emotion, validatedPageable);
        return entryMapper.toResponseDtoPage(entryPage);
    }

    @Transactional(readOnly = true)
    public EntryResponseDto getEntryById(String userId, String entryId) {
        EntryEntity entry = entryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new NotFoundException("Entry not found"));
        
        return entryMapper.toResponseDto(entry);
    }

    public EntryResponseDto createEntry(String userId, EntryRequestDto requestDto) {
        if (requestDto.getEmotions() == null || requestDto.getEmotions().isEmpty()) {
            throw new ValidationException("At least one emotion is required");
        }

        String entryId = UUID.randomUUID().toString();
        EntryEntity entry = entryMapper.toEntity(requestDto, entryId, userId);
        
        EntryEntity savedEntry = entryRepository.save(entry);
        return entryMapper.toResponseDto(savedEntry);
    }

    public EntryResponseDto updateEntry(String userId, String entryId, EntryRequestDto requestDto) {
        EntryEntity entry = entryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new NotFoundException("Entry not found"));

        if (requestDto.getEmotions() == null || requestDto.getEmotions().isEmpty()) {
            throw new ValidationException("At least one emotion is required");
        }

        entryMapper.updateEntityFromDto(requestDto, entry);

        EntryEntity savedEntry = entryRepository.save(entry);
        return entryMapper.toResponseDto(savedEntry);
    }

    public void deleteEntry(String userId, String entryId) {
        if (!entryRepository.existsByIdAndUserId(entryId, userId)) {
            throw new NotFoundException("Entry not found");
        }

        entryRepository.deleteById(entryId);
    }

    private Pageable validatePageable(Pageable pageable) {
        int page = Math.max(0, pageable.getPageNumber());
        int pageSize = Math.min(Math.max(1, pageable.getPageSize()), MAX_PAGE_SIZE);
        return PageRequest.of(page, pageSize, pageable.getSort());
    }

    private LocalDateTime parseDateTime(String dateString) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return LocalDateTime.parse(dateString, formatter);
    }
}
