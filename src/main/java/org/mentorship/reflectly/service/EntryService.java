package org.mentorship.reflectly.service;

import lombok.RequiredArgsConstructor;

import org.mentorship.reflectly.dto.EntryRequestDto;
import org.mentorship.reflectly.dto.EntryResponseDto;
import org.mentorship.reflectly.exception.NotFoundException;
import org.mentorship.reflectly.exception.ValidationException;
import org.mentorship.reflectly.model.EntryEntity;
import org.mentorship.reflectly.repository.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class EntryService {

    private final EntryRepository entryRepository;

    @Transactional(readOnly = true)
    public List<EntryResponseDto> getAllEntries(String userId) {
        List<EntryEntity> entries = entryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return entries.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EntryResponseDto> getEntriesByDateRange(String userId, String startDate, String endDate) {
        try {
            LocalDateTime start = parseDateTime(startDate);
            LocalDateTime end = parseDateTime(endDate);
            
            List<EntryEntity> entries = entryRepository.findByUserIdAndCreatedAtBetween(userId, start, end);
            return entries.stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date format. Use ISO format (yyyy-MM-ddTHH:mm:ss)");
        }
    }

    @Transactional(readOnly = true)
    public List<EntryResponseDto> getEntriesByEmotion(String userId, String emotion) {
        List<EntryEntity> entries = entryRepository.findByUserIdAndEmotionsContaining(userId, emotion);
        return entries.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
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
                requestDto.getEmotions(),
                requestDto.getActivities()
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
        
        entry.getEmotions().clear();
        if (requestDto.getEmotions() != null) {
            entry.getEmotions().addAll(requestDto.getEmotions());
        }
        
        entry.getActivities().clear();
        if (requestDto.getActivities() != null) {
            entry.getActivities().addAll(requestDto.getActivities());
        }

        EntryEntity savedEntry = entryRepository.save(entry);
        return convertToResponseDto(savedEntry);
    }

    public void deleteEntry(String userId, String entryId) {
        if (!entryRepository.existsByIdAndUserId(entryId, userId)) {
            throw new NotFoundException("Entry not found");
        }

        entryRepository.deleteById(entryId);
    }

    private EntryResponseDto convertToResponseDto(EntryEntity entity) {
        return EntryResponseDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .reflection(entity.getReflection())
                .emotions(entity.getEmotions())
                .activities(entity.getActivities())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private LocalDateTime parseDateTime(String dateString) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return LocalDateTime.parse(dateString, formatter);
    }
}
