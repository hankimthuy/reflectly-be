package org.mentorship.reflectly.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.constants.ApiConstants;
import org.mentorship.reflectly.dto.ConversationResponseDto;
import org.mentorship.reflectly.dto.PagedResponseDto;
import org.mentorship.reflectly.dto.SendMessageRequestDto;
import org.mentorship.reflectly.dto.SendMessageResponseDto;
import org.mentorship.reflectly.security.GoogleAuthenticationToken;
import org.mentorship.reflectly.service.ConversationService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/conversations")
@Tag(name = "Conversations", description = "Coach chat sessions")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @Operation(summary = "Start a Coach conversation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.CREATED, description = "Conversation started"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @PostMapping
    public ResponseEntity<ConversationResponseDto> startConversation(GoogleAuthenticationToken authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        ConversationResponseDto conversation = conversationService.startConversation(userId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(conversation.getId())
                .toUri();
        return ResponseEntity.created(location).body(conversation);
    }

    @Operation(summary = "List conversations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Conversations retrieved successfully"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @GetMapping
    public ResponseEntity<PagedResponseDto<ConversationResponseDto>> getAllConversations(
            GoogleAuthenticationToken authentication,
            @ParameterObject Pageable pageable) {
        Long userId = getUserIdFromAuthentication(authentication);
        Page<ConversationResponseDto> page = conversationService.getAllConversations(userId, pageable);

        String nextLink = null;
        if (page.hasNext()) {
            nextLink = ServletUriComponentsBuilder.fromCurrentRequest()
                    .replaceQueryParam("page", page.getNumber() + 1)
                    .toUriString();
        }

        return ResponseEntity.ok(new PagedResponseDto<>(page.getContent(), page.getTotalElements(), nextLink));
    }

    @Operation(summary = "Get a conversation with its full message history")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Conversation retrieved successfully"),
            @ApiResponse(responseCode = ApiConstants.NOT_FOUND, description = "Conversation not found"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ConversationResponseDto> getConversationById(
            @Parameter(description = "Conversation ID") @PathVariable String id,
            GoogleAuthenticationToken authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(conversationService.getConversationById(userId, id));
    }

    @Operation(summary = "Send a message to the Coach",
            description = "Persists the user's message and returns both it and the Coach's reply. "
                    + "The persisted user message carries the server-computed mood reading "
                    + "(moodEmotion/moodScore), so callers should replace their optimistic copy with it.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Reply generated successfully"),
            @ApiResponse(responseCode = ApiConstants.BAD_REQUEST, description = "Validation error or conversation not active"),
            @ApiResponse(responseCode = ApiConstants.NOT_FOUND, description = "Conversation not found"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @PostMapping("/{id}/messages")
    public ResponseEntity<SendMessageResponseDto> sendMessage(
            @Parameter(description = "Conversation ID") @PathVariable String id,
            @Valid @RequestBody SendMessageRequestDto requestDto,
            GoogleAuthenticationToken authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(conversationService.sendMessage(userId, id, requestDto.getContent()));
    }

    @Operation(summary = "End a conversation", description = "Marks the session ended and triggers background memory extraction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Conversation ended successfully"),
            @ApiResponse(responseCode = ApiConstants.NOT_FOUND, description = "Conversation not found"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @PostMapping("/{id}/end")
    public ResponseEntity<ConversationResponseDto> endConversation(
            @Parameter(description = "Conversation ID") @PathVariable String id,
            GoogleAuthenticationToken authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(conversationService.endConversation(userId, id));
    }

    @Operation(summary = "Summarize a conversation", description = "Generates (and persists) a markdown recap of the conversation so far; callable any time, safe to re-call to regenerate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = ApiConstants.SUCCESS, description = "Summary generated successfully"),
            @ApiResponse(responseCode = ApiConstants.NOT_FOUND, description = "Conversation not found"),
            @ApiResponse(responseCode = ApiConstants.UNAUTHORIZED, description = "Invalid or missing authentication token")
    })
    @PostMapping("/{id}/summarize")
    public ResponseEntity<ConversationResponseDto> summarizeConversation(
            @Parameter(description = "Conversation ID") @PathVariable String id,
            GoogleAuthenticationToken authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(conversationService.summarizeConversation(userId, id));
    }

    private Long getUserIdFromAuthentication(GoogleAuthenticationToken authentication) {
        if (authentication != null && authentication.getUser() != null) {
            return authentication.getUser().getId();
        }
        throw new RuntimeException(ApiConstants.USER_NOT_AUTHENTICATED);
    }
}
