package org.mentorship.reflectly.controller;

import org.mentorship.reflectly.constants.ApiConstants;
import org.mentorship.reflectly.dto.EntryRequestDto;
import org.mentorship.reflectly.dto.EntryResponseDto;
import org.mentorship.reflectly.dto.PagedResponseDto;
import org.mentorship.reflectly.exception.ValidationException;
import org.mentorship.reflectly.security.GoogleAuthenticationToken;
import org.mentorship.reflectly.service.EntryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.net.URI;
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
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;

    @Operation(summary = "Get all entries", description = "Get all entries for the current user with optional filtering by date range or emotion. Note: Both startDate and endDate must be provided together for date range filtering.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Entries retrieved successfully"),
            @ApiResponse(responseCode = ApiConstants.BAD_REQUEST, description = "Validation error"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @GetMapping
    public ResponseEntity<PagedResponseDto<EntryResponseDto>> getAllEntries(
            GoogleAuthenticationToken authentication,
            @ParameterObject Pageable pageable) {

        String userId = getUserIdFromAuthentication(authentication);
        Page<EntryResponseDto> pageResult = entryService.getAllEntries(userId, pageable);

        // Generate nextLink
        String nextLink = null;
        if (pageResult.hasNext()) {
            // Build URL from current request, incrementing page number
            nextLink = ServletUriComponentsBuilder.fromCurrentRequest()
                    .replaceQueryParam("page", pageResult.getNumber() + 1)
                    .toUriString();
        }

        return ResponseEntity.ok(new PagedResponseDto<>(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                nextLink
        ));
    }

    @Operation(summary = "Get entry by ID", description = "Get a specific entry by its ID for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Entry retrieved successfully"),
            @ApiResponse(responseCode = ApiConstants.NOT_FOUND, description = "Entry not found"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntryResponseDto> getEntryById(
            @Parameter(description = "Entry ID") @PathVariable String id,
            GoogleAuthenticationToken authentication) {

        String userId = getUserIdFromAuthentication(authentication);
        EntryResponseDto entry = entryService.getEntryById(userId, id);
        return ResponseEntity.ok(entry);
    }

    @Operation(summary = "Create new entry", description = "Create a new entry for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.CREATED, description = "Entry created successfully"),
            @ApiResponse(responseCode = ApiConstants.BAD_REQUEST, description = "Validation error"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @PostMapping
    public ResponseEntity<EntryResponseDto> createEntry(
            @Valid @RequestBody EntryRequestDto requestDto,
            GoogleAuthenticationToken authentication) {

        String userId = getUserIdFromAuthentication(authentication);
        EntryResponseDto entry = entryService.createEntry(userId, requestDto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(entry.getId())
                .toUri();
        return ResponseEntity.created(location).body(entry);
    }

    @Operation(summary = "Update entry", description = "Update an existing entry for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Entry updated successfully"),
            @ApiResponse(responseCode = ApiConstants.BAD_REQUEST, description = "Validation error"),
            @ApiResponse(responseCode = ApiConstants.NOT_FOUND, description = "Entry not found"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntryResponseDto> updateEntry(
            @Parameter(description = "Entry ID") @PathVariable String id,
            @Valid @RequestBody EntryRequestDto requestDto,
            GoogleAuthenticationToken authentication) {

        String userId = getUserIdFromAuthentication(authentication);
        EntryResponseDto entry = entryService.updateEntry(userId, id, requestDto);
        return ResponseEntity.ok(entry);
    }

    @Operation(summary = "Delete entry", description = "Delete an entry for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.NO_CONTENT, description = "Entry deleted successfully"),
            @ApiResponse(responseCode = ApiConstants.NOT_FOUND, description = "Entry not found"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(
            @Parameter(description = "Entry ID") @PathVariable String id, GoogleAuthenticationToken authentication) {
        String userId = getUserIdFromAuthentication(authentication);
        entryService.deleteEntry(userId, id);
        return ResponseEntity.noContent().build();
    }

    private String getUserIdFromAuthentication(GoogleAuthenticationToken authentication) {
        if (authentication != null && authentication.getUser() != null) {
            return authentication.getUser().getId().toString();
        }
        throw new RuntimeException(ApiConstants.USER_NOT_AUTHENTICATED);
    }
}