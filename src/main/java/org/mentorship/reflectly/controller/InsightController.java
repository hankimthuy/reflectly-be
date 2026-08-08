package org.mentorship.reflectly.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.constants.ApiConstants;
import org.mentorship.reflectly.dto.InsightResponseDto;
import org.mentorship.reflectly.dto.PagedResponseDto;
import org.mentorship.reflectly.security.GoogleAuthenticationToken;
import org.mentorship.reflectly.service.InsightService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/insights")
@Tag(name = "Insights", description = "Timeline of insights derived from Coach conversations")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    @Operation(summary = "List insights", description = "Get the current user's insight timeline, paginated, newest first, optionally filtered to one person")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Insights retrieved successfully"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @GetMapping
    public ResponseEntity<PagedResponseDto<InsightResponseDto>> getAllInsights(
            GoogleAuthenticationToken authentication,
            @RequestParam(required = false) String personId,
            @ParameterObject Pageable pageable) {
        Long userId = getUserIdFromAuthentication(authentication);
        Page<InsightResponseDto> pageResult = insightService.getAllInsights(userId, personId, pageable);

        String nextLink = null;
        if (pageResult.hasNext()) {
            nextLink = ServletUriComponentsBuilder.fromCurrentRequest()
                    .replaceQueryParam("page", pageResult.getNumber() + 1)
                    .toUriString();
        }

        return ResponseEntity.ok(new PagedResponseDto<>(pageResult.getContent(), pageResult.getTotalElements(), nextLink));
    }

    private Long getUserIdFromAuthentication(GoogleAuthenticationToken authentication) {
        if (authentication != null && authentication.getUser() != null) {
            return authentication.getUser().getId();
        }
        throw new RuntimeException(ApiConstants.USER_NOT_AUTHENTICATED);
    }
}
