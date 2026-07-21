package org.mentorship.reflectly.controller;

import org.mentorship.reflectly.constants.ApiConstants;
import org.mentorship.reflectly.dto.EnergyLogRequestDto;
import org.mentorship.reflectly.dto.EnergyLogResponseDto;
import org.mentorship.reflectly.dto.PagedResponseDto;
import org.mentorship.reflectly.security.GoogleAuthenticationToken;
import org.mentorship.reflectly.service.EnergyLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.net.URI;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
@RequestMapping("/api/energy-logs")
@RequiredArgsConstructor
public class EnergyLogController {

    private final EnergyLogService energyLogService;

    @Operation(summary = "Get energy logs", description = "Get all energy logs for the current user, optionally filtered by context tag")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Energy logs retrieved successfully"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @GetMapping
    public ResponseEntity<PagedResponseDto<EnergyLogResponseDto>> getAllLogs(
            @Parameter(description = "Filter by context tag") @RequestParam(required = false) String contextTag,
            GoogleAuthenticationToken authentication,
            @ParameterObject Pageable pageable) {

        String userId = getUserIdFromAuthentication(authentication);
        Page<EnergyLogResponseDto> pageResult = energyLogService.getAllLogs(userId, contextTag, pageable);

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

    @Operation(summary = "Get energy logs in range", description = "Get raw energy-log points for the last N days (default 30), oldest first, for dashboard trend aggregation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Energy logs retrieved successfully"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @GetMapping("/range")
    public ResponseEntity<List<EnergyLogResponseDto>> getLogsInRange(
            @Parameter(description = "Number of days back from now (default 30, max 366)") @RequestParam(required = false) Integer days,
            GoogleAuthenticationToken authentication) {

        String userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(energyLogService.getLogsInRange(userId, days));
    }

    @Operation(summary = "Log energy level", description = "Create a new energy log entry for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.CREATED, description = "Energy log created successfully"),
            @ApiResponse(responseCode = ApiConstants.BAD_REQUEST, description = "Validation error"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @PostMapping
    public ResponseEntity<EnergyLogResponseDto> createLog(
            @Valid @RequestBody EnergyLogRequestDto requestDto,
            GoogleAuthenticationToken authentication) {

        String userId = getUserIdFromAuthentication(authentication);
        EnergyLogResponseDto log = energyLogService.createLog(userId, requestDto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(log.getId())
                .toUri();
        return ResponseEntity.created(location).body(log);
    }

    @Operation(summary = "Delete energy log", description = "Delete an energy log for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.NO_CONTENT, description = "Energy log deleted successfully"),
            @ApiResponse(responseCode = ApiConstants.NOT_FOUND, description = "Energy log not found"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(
            @Parameter(description = "Energy log ID") @PathVariable String id, GoogleAuthenticationToken authentication) {
        String userId = getUserIdFromAuthentication(authentication);
        energyLogService.deleteLog(userId, id);
        return ResponseEntity.noContent().build();
    }

    private String getUserIdFromAuthentication(GoogleAuthenticationToken authentication) {
        if (authentication != null && authentication.getUser() != null) {
            return authentication.getUser().getId().toString();
        }
        throw new RuntimeException(ApiConstants.USER_NOT_AUTHENTICATED);
    }
}
