package org.mentorship.reflectly.service;

import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.DTO.ApiResponseDto;
import org.mentorship.reflectly.DTO.EntryRequestDto;
import org.mentorship.reflectly.DTO.EntryResponseDto;
import org.mentorship.reflectly.constants.ApiResponseCodes;
import org.mentorship.reflectly.model.EntryEntity;
import org.mentorship.reflectly.repository.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class EntryService {

    private final EntryRepository entryRepository;

    /**
     * Get all entries for a specific user.
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<List<EntryResponseDto>> getAllEntries(String userId) {
        try {
            List<EntryEntity> entries = entryRepository.findByUserIdOrderByCreatedAtDesc(userId);
            List<EntryResponseDto> responseDtos = entries.stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());
            
            return ApiResponseDto.success(responseDtos, ApiResponseCodes.ENTRIES_RETRIEVED);
        } catch (Exception e) {
            return ApiResponseDto.error(createErrorDto(ApiResponseCodes.INTERNAL_SERVER_ERROR, 
                    ApiResponseCodes.INTERNAL_SERVER_ERROR_MESSAGE, e.getMessage()));
        }
    }

    /**
     * Get entries by date range for a specific user.
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<List<EntryResponseDto>> getEntriesByDateRange(String userId, String startDate, String endDate) {
        try {
            LocalDateTime start = parseDateTime(startDate);
            LocalDateTime end = parseDateTime(endDate);
            
            List<EntryEntity> entries = entryRepository.findByUserIdAndCreatedAtBetween(userId, start, end);
            List<EntryResponseDto> responseDtos = entries.stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());
            
            return ApiResponseDto.success(responseDtos, ApiResponseCodes.ENTRIES_RETRIEVED);
        } catch (DateTimeParseException e) {
            return ApiResponseDto.error(createErrorDto(ApiResponseCodes.VALIDATION_ERROR, 
                    "Invalid date format. Use ISO format (yyyy-MM-ddTHH:mm:ss)", null));
        } catch (Exception e) {
            return ApiResponseDto.error(createErrorDto(ApiResponseCodes.INTERNAL_SERVER_ERROR, 
                    ApiResponseCodes.INTERNAL_SERVER_ERROR_MESSAGE, e.getMessage()));
        }
    }

    /**
     * Get entries by emotion for a specific user.
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<List<EntryResponseDto>> getEntriesByEmotion(String userId, String emotion) {
        try {
            List<EntryEntity> entries = entryRepository.findByUserIdAndEmotionsContaining(userId, emotion);
            List<EntryResponseDto> responseDtos = entries.stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());
            
            return ApiResponseDto.success(responseDtos, ApiResponseCodes.ENTRIES_RETRIEVED);
        } catch (Exception e) {
            return ApiResponseDto.error(createErrorDto(ApiResponseCodes.INTERNAL_SERVER_ERROR, 
                    ApiResponseCodes.INTERNAL_SERVER_ERROR_MESSAGE, e.getMessage()));
        }
    }

    /**
     * Get a specific entry by ID for a specific user.
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<EntryResponseDto> getEntryById(String userId, String entryId) {
        try {
            Optional<EntryEntity> entry = entryRepository.findByIdAndUserId(entryId, userId);
            if (entry.isEmpty()) {
                return ApiResponseDto.error(createErrorDto(ApiResponseCodes.ENTRY_NOT_FOUND, 
                        ApiResponseCodes.ENTRY_NOT_FOUND_MESSAGE, null));
            }
            
            return ApiResponseDto.success(convertToResponseDto(entry.get()), ApiResponseCodes.ENTRY_RETRIEVED);
        } catch (Exception e) {
            return ApiResponseDto.error(createErrorDto(ApiResponseCodes.INTERNAL_SERVER_ERROR, 
                    ApiResponseCodes.INTERNAL_SERVER_ERROR_MESSAGE, e.getMessage()));
        }
    }

    /**
     * Create a new entry for a specific user.
     */
    public ApiResponseDto<EntryResponseDto> createEntry(String userId, EntryRequestDto requestDto) {
        try {
            // Validate emotions list
            if (requestDto.getEmotions() == null || requestDto.getEmotions().isEmpty()) {
                return ApiResponseDto.error(createErrorDto(ApiResponseCodes.VALIDATION_ERROR, 
                        "At least one emotion is required", null));
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
            
            return ApiResponseDto.success(convertToResponseDto(savedEntry), ApiResponseCodes.ENTRY_CREATED);
        } catch (Exception e) {
            return ApiResponseDto.error(createErrorDto(ApiResponseCodes.INTERNAL_SERVER_ERROR, 
                    ApiResponseCodes.INTERNAL_SERVER_ERROR_MESSAGE, e.getMessage()));
        }
    }

    /**
     * Update an existing entry for a specific user.
     */
    public ApiResponseDto<EntryResponseDto> updateEntry(String userId, String entryId, EntryRequestDto requestDto) {
        try {
            Optional<EntryEntity> existingEntry = entryRepository.findByIdAndUserId(entryId, userId);
            if (existingEntry.isEmpty()) {
                return ApiResponseDto.error(createErrorDto(ApiResponseCodes.ENTRY_NOT_FOUND, 
                        ApiResponseCodes.ENTRY_NOT_FOUND_MESSAGE, null));
            }

            // Validate emotions list
            if (requestDto.getEmotions() == null || requestDto.getEmotions().isEmpty()) {
                return ApiResponseDto.error(createErrorDto(ApiResponseCodes.VALIDATION_ERROR, 
                        "At least one emotion is required", null));
            }

            EntryEntity entry = existingEntry.get();
            entry.updateDetails(
                    requestDto.getTitle(),
                    requestDto.getReflection(),
                    requestDto.getEmotions(),
                    requestDto.getActivities()
            );

            EntryEntity savedEntry = entryRepository.save(entry);
            return ApiResponseDto.success(convertToResponseDto(savedEntry), ApiResponseCodes.ENTRY_UPDATED);
        } catch (Exception e) {
            return ApiResponseDto.error(createErrorDto(ApiResponseCodes.INTERNAL_SERVER_ERROR, 
                    ApiResponseCodes.INTERNAL_SERVER_ERROR_MESSAGE, e.getMessage()));
        }
    }

    /**
     * Delete an entry for a specific user.
     */
    public ApiResponseDto<Void> deleteEntry(String userId, String entryId) {
        try {
            if (!entryRepository.existsByIdAndUserId(entryId, userId)) {
                return ApiResponseDto.error(createErrorDto(ApiResponseCodes.ENTRY_NOT_FOUND, 
                        ApiResponseCodes.ENTRY_NOT_FOUND_MESSAGE, null));
            }

            entryRepository.deleteById(entryId);
            return ApiResponseDto.success(ApiResponseCodes.ENTRY_DELETED);
        } catch (Exception e) {
            return ApiResponseDto.error(createErrorDto(ApiResponseCodes.INTERNAL_SERVER_ERROR, 
                    ApiResponseCodes.INTERNAL_SERVER_ERROR_MESSAGE, e.getMessage()));
        }
    }

    /**
     * Convert EntryEntity to EntryResponseDto.
     */
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

    /**
     * Parse date string to LocalDateTime.
     */
    private LocalDateTime parseDateTime(String dateString) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return LocalDateTime.parse(dateString, formatter);
    }

    /**
     * Create error DTO.
     */
    private ApiResponseDto.ErrorDto createErrorDto(String code, String message, Object details) {
        return ApiResponseDto.ErrorDto.builder()
                .code(code)
                .message(message)
                .details(details)
                .build();
    }
}