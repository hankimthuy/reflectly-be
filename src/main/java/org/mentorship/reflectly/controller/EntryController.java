package org.mentorship.reflectly.controller;

import java.util.List;

import org.mentorship.reflectly.DTO.ApiResponseDto;
import org.mentorship.reflectly.DTO.EntryRequestDto;
import org.mentorship.reflectly.DTO.EntryResponseDto;
import org.mentorship.reflectly.security.GoogleAuthenticationToken;
import org.mentorship.reflectly.service.EntryService;
import org.mentorship.reflectly.util.HttpStatusUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for managing entries (journal entries).
 * Provides CRUD operations and filtering capabilities for entries.
 */
@RestController
@RequestMapping("/api/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;

    /**
     * GET /api/entries
     * Get all entries for the current user.
     */
    @Operation(
        summary = "Get all entries", 
        description = "Get all entries for the current user with optional filtering by date range or emotion"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Entries retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication token")
    })
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<EntryResponseDto>>> getAllEntries(
            GoogleAuthenticationToken authentication,
            @Parameter(description = "Start date for filtering (ISO format)") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date for filtering (ISO format)") @RequestParam(required = false) String endDate,
            @Parameter(description = "Emotion to filter by") @RequestParam(required = false) String emotion) {
        
        String userId = getUserIdFromAuthentication(authentication);
        
        // Filter by date range if provided
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(entryService.getEntriesByDateRange(userId, startDate, endDate));
        }
        
        // Filter by emotion if provided
        if (emotion != null) {
            return ResponseEntity.ok(entryService.getEntriesByEmotion(userId, emotion));
        }
        
        // Get all entries
        return ResponseEntity.ok(entryService.getAllEntries(userId));
    }

    /**
     * GET /api/entries/:id
     * Get a specific entry by ID.
     */
    @Operation(
        summary = "Get entry by ID", 
        description = "Get a specific entry by its ID for the current user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Entry retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Entry not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication token")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<EntryResponseDto>> getEntryById(
            @Parameter(description = "Entry ID") @PathVariable String id,
            GoogleAuthenticationToken authentication) {
        
        String userId = getUserIdFromAuthentication(authentication);
        ApiResponseDto<EntryResponseDto> response = entryService.getEntryById(userId, id);
        
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/entries
     * Create a new entry.
     */
    @Operation(
        summary = "Create new entry", 
        description = "Create a new entry for the current user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Entry created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication token")
    })
    @PostMapping
    public ResponseEntity<ApiResponseDto<EntryResponseDto>> createEntry(
            @Valid @RequestBody EntryRequestDto requestDto,
            GoogleAuthenticationToken authentication) {
    
        String userId = getUserIdFromAuthentication(authentication);
     
        ApiResponseDto<EntryResponseDto> response = entryService.createEntry(userId, requestDto);
        
        if (!response.isSuccess()) {
            HttpStatus status = HttpStatusUtils.getHttpStatusFromErrorCode(response.getError().getCode());
            return ResponseEntity.status(status).body(response);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/entries/:id
     * Update an existing entry.
     */
    @Operation(
        summary = "Update entry", 
        description = "Update an existing entry for the current user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Entry updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Entry not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication token")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<EntryResponseDto>> updateEntry(
            @Parameter(description = "Entry ID") @PathVariable String id,
            @Valid @RequestBody EntryRequestDto requestDto,
            GoogleAuthenticationToken authentication) {
        
        String userId = getUserIdFromAuthentication(authentication);
        ApiResponseDto<EntryResponseDto> response = entryService.updateEntry(userId, id, requestDto);
        
        if (!response.isSuccess()) {
            HttpStatus status = HttpStatusUtils.getHttpStatusFromErrorCode(response.getError().getCode());
            return ResponseEntity.status(status).body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/entries/:id
     * Delete an entry.
     */
    @Operation(
        summary = "Delete entry", 
        description = "Delete an entry for the current user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Entry deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Entry not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication token")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteEntry(
            @Parameter(description = "Entry ID") @PathVariable String id,
            GoogleAuthenticationToken authentication) {
        
        String userId = getUserIdFromAuthentication(authentication);
        ApiResponseDto<Void> response = entryService.deleteEntry(userId, id);
        
        if (!response.isSuccess()) {
            HttpStatus status = HttpStatusUtils.getHttpStatusFromErrorCode(response.getError().getCode());
            return ResponseEntity.status(status).body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    private String getUserIdFromAuthentication(GoogleAuthenticationToken authentication) {
        if (authentication != null && authentication.getUser() != null) {
            // Extract user ID from the UserEntity in the authentication token
            return authentication.getUser().getId().toString();
        }
        throw new RuntimeException("User not authenticated");
    }

}
