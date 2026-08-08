package org.mentorship.reflectly.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.constants.ApiConstants;
import org.mentorship.reflectly.dto.PagedResponseDto;
import org.mentorship.reflectly.dto.SavedFrameworkEntryRequestDto;
import org.mentorship.reflectly.dto.SavedFrameworkEntryResponseDto;
import org.mentorship.reflectly.dto.SavedFrameworkEntryUpdateRequestDto;
import org.mentorship.reflectly.model.FrameworkType;
import org.mentorship.reflectly.security.GoogleAuthenticationToken;
import org.mentorship.reflectly.service.SavedFrameworkEntryService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/saved-framework-entries")
@Tag(name = "Saved Framework Entries", description = "User-captured, editable structured insights (Free-form, Johari Window, ...) shown on Đúc kết")
@RequiredArgsConstructor
public class SavedFrameworkEntryController {

    private final SavedFrameworkEntryService savedFrameworkEntryService;

    @Operation(summary = "List saved framework entries", description = "Paginated, newest first, optionally filtered by frameworkType")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Entries retrieved successfully"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @GetMapping
    public ResponseEntity<PagedResponseDto<SavedFrameworkEntryResponseDto>> getAllEntries(
            GoogleAuthenticationToken authentication,
            @Parameter(description = "Optional filter by framework type") @RequestParam(required = false) FrameworkType frameworkType,
            @ParameterObject Pageable pageable) {
        Long userId = getUserIdFromAuthentication(authentication);
        Page<SavedFrameworkEntryResponseDto> page = savedFrameworkEntryService.getAllEntries(userId, frameworkType, pageable);

        String nextLink = null;
        if (page.hasNext()) {
            nextLink = ServletUriComponentsBuilder.fromCurrentRequest()
                    .replaceQueryParam("page", page.getNumber() + 1)
                    .toUriString();
        }

        return ResponseEntity.ok(new PagedResponseDto<>(page.getContent(), page.getTotalElements(), nextLink));
    }

    @Operation(summary = "Get a saved framework entry by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Entry retrieved successfully"),
            @ApiResponse(responseCode = ApiConstants.NOT_FOUND, description = "Entry not found"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SavedFrameworkEntryResponseDto> getEntryById(
            @Parameter(description = "Entry ID") @PathVariable String id,
            GoogleAuthenticationToken authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(savedFrameworkEntryService.getEntryById(userId, id));
    }

    @Operation(summary = "Save a framework entry", description = "Captures a structured insight (Free-form, Johari Window, ...), optionally linked to a Coach conversation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.CREATED, description = "Entry created successfully"),
            @ApiResponse(responseCode = ApiConstants.BAD_REQUEST, description = "Validation error"),
            @ApiResponse(responseCode = ApiConstants.NOT_FOUND, description = "Referenced conversation not found"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @PostMapping
    public ResponseEntity<SavedFrameworkEntryResponseDto> createEntry(
            @Valid @RequestBody SavedFrameworkEntryRequestDto requestDto,
            GoogleAuthenticationToken authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        SavedFrameworkEntryResponseDto entry = savedFrameworkEntryService.createEntry(userId, requestDto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(entry.getId())
                .toUri();
        return ResponseEntity.created(location).body(entry);
    }

    @Operation(summary = "Update a saved framework entry", description = "Edits title/payload; the framework type and source conversation are immutable")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Entry updated successfully"),
            @ApiResponse(responseCode = ApiConstants.BAD_REQUEST, description = "Validation error"),
            @ApiResponse(responseCode = ApiConstants.NOT_FOUND, description = "Entry not found"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SavedFrameworkEntryResponseDto> updateEntry(
            @Parameter(description = "Entry ID") @PathVariable String id,
            @Valid @RequestBody SavedFrameworkEntryUpdateRequestDto requestDto,
            GoogleAuthenticationToken authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(savedFrameworkEntryService.updateEntry(userId, id, requestDto));
    }

    @Operation(summary = "Delete a saved framework entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.NO_CONTENT, description = "Entry deleted successfully"),
            @ApiResponse(responseCode = ApiConstants.NOT_FOUND, description = "Entry not found"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(
            @Parameter(description = "Entry ID") @PathVariable String id,
            GoogleAuthenticationToken authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        savedFrameworkEntryService.deleteEntry(userId, id);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromAuthentication(GoogleAuthenticationToken authentication) {
        if (authentication != null && authentication.getUser() != null) {
            return authentication.getUser().getId();
        }
        throw new RuntimeException(ApiConstants.USER_NOT_AUTHENTICATED);
    }
}
