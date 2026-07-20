package org.mentorship.reflectly.controller;

import org.mentorship.reflectly.constants.ApiConstants;
import org.mentorship.reflectly.dto.ActionProtocolRequestDto;
import org.mentorship.reflectly.dto.ActionProtocolResponseDto;
import org.mentorship.reflectly.dto.MarkProtocolUsedRequestDto;
import org.mentorship.reflectly.dto.PagedResponseDto;
import org.mentorship.reflectly.security.GoogleAuthenticationToken;
import org.mentorship.reflectly.service.ActionProtocolService;
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
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/protocols")
@RequiredArgsConstructor
public class ActionProtocolController {

    private final ActionProtocolService actionProtocolService;

    @Operation(summary = "Get action protocols", description = "Get all action protocols for the current user, paginated")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Action protocols retrieved successfully"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @GetMapping
    public ResponseEntity<PagedResponseDto<ActionProtocolResponseDto>> getAllProtocols(
            GoogleAuthenticationToken authentication,
            @ParameterObject Pageable pageable) {

        String userId = getUserIdFromAuthentication(authentication);
        Page<ActionProtocolResponseDto> pageResult = actionProtocolService.getAllProtocols(userId, pageable);

        String nextLink = null;
        if (pageResult.hasNext()) {
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

    @Operation(summary = "Get action protocol by ID", description = "Get a specific action protocol by its ID for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Action protocol retrieved successfully"),
            @ApiResponse(responseCode = ApiConstants.NOT_FOUND, description = "Action protocol not found"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ActionProtocolResponseDto> getProtocolById(
            @Parameter(description = "Action protocol ID") @PathVariable String id,
            GoogleAuthenticationToken authentication) {

        String userId = getUserIdFromAuthentication(authentication);
        ActionProtocolResponseDto protocol = actionProtocolService.getProtocolById(userId, id);
        return ResponseEntity.ok(protocol);
    }

    @Operation(summary = "Create action protocol", description = "Create a new action protocol for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.CREATED, description = "Action protocol created successfully"),
            @ApiResponse(responseCode = ApiConstants.BAD_REQUEST, description = "Validation error"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @PostMapping
    public ResponseEntity<ActionProtocolResponseDto> createProtocol(
            @Valid @RequestBody ActionProtocolRequestDto requestDto,
            GoogleAuthenticationToken authentication) {

        String userId = getUserIdFromAuthentication(authentication);
        ActionProtocolResponseDto protocol = actionProtocolService.createProtocol(userId, requestDto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(protocol.getId())
                .toUri();
        return ResponseEntity.created(location).body(protocol);
    }

    @Operation(summary = "Update action protocol", description = "Update an existing action protocol for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Action protocol updated successfully"),
            @ApiResponse(responseCode = ApiConstants.BAD_REQUEST, description = "Validation error"),
            @ApiResponse(responseCode = ApiConstants.NOT_FOUND, description = "Action protocol not found"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ActionProtocolResponseDto> updateProtocol(
            @Parameter(description = "Action protocol ID") @PathVariable String id,
            @Valid @RequestBody ActionProtocolRequestDto requestDto,
            GoogleAuthenticationToken authentication) {

        String userId = getUserIdFromAuthentication(authentication);
        ActionProtocolResponseDto protocol = actionProtocolService.updateProtocol(userId, id, requestDto);
        return ResponseEntity.ok(protocol);
    }

    @Operation(summary = "Delete action protocol", description = "Delete an action protocol for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.NO_CONTENT, description = "Action protocol deleted successfully"),
            @ApiResponse(responseCode = ApiConstants.NOT_FOUND, description = "Action protocol not found"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProtocol(
            @Parameter(description = "Action protocol ID") @PathVariable String id, GoogleAuthenticationToken authentication) {
        String userId = getUserIdFromAuthentication(authentication);
        actionProtocolService.deleteProtocol(userId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Mark action protocol as used", description = "Records a use of the protocol: increments its usage count, stamps lastUsedAt, and logs the effectiveness")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Action protocol usage recorded successfully"),
            @ApiResponse(responseCode = ApiConstants.BAD_REQUEST, description = "Validation error"),
            @ApiResponse(responseCode = ApiConstants.NOT_FOUND, description = "Action protocol not found"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @PostMapping("/{id}/use")
    public ResponseEntity<ActionProtocolResponseDto> markUsed(
            @Parameter(description = "Action protocol ID") @PathVariable String id,
            @Valid @RequestBody MarkProtocolUsedRequestDto requestDto,
            GoogleAuthenticationToken authentication) {

        String userId = getUserIdFromAuthentication(authentication);
        ActionProtocolResponseDto protocol = actionProtocolService.markUsed(userId, id, requestDto);
        return ResponseEntity.ok(protocol);
    }

    private String getUserIdFromAuthentication(GoogleAuthenticationToken authentication) {
        if (authentication != null && authentication.getUser() != null) {
            return authentication.getUser().getId().toString();
        }
        throw new RuntimeException(ApiConstants.USER_NOT_AUTHENTICATED);
    }
}
