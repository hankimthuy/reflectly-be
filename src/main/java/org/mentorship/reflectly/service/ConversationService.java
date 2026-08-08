package org.mentorship.reflectly.service;

import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.ai.CoachAgentService;
import org.mentorship.reflectly.ai.ConversationEndedEvent;
import org.mentorship.reflectly.ai.ConversationSummaryService;
import org.mentorship.reflectly.converter.ConversationConverter;
import org.mentorship.reflectly.dto.ConversationMessageResponseDto;
import org.mentorship.reflectly.dto.ConversationResponseDto;
import org.mentorship.reflectly.exception.NotFoundException;
import org.mentorship.reflectly.exception.ValidationException;
import org.mentorship.reflectly.model.*;
import org.mentorship.reflectly.repository.ConversationMessageRepository;
import org.mentorship.reflectly.repository.ConversationRepository;
import org.mentorship.reflectly.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final UserRepository userRepository;
    private final ConversationConverter conversationConverter;
    private final CoachAgentService coachAgentService;
    private final ConversationSummaryService conversationSummaryService;
    private final ApplicationEventPublisher eventPublisher;

    public ConversationResponseDto startConversation(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ConversationEntity conversation = new ConversationEntity(UUID.randomUUID().toString(), user);
        conversationRepository.save(conversation);
        return conversationConverter.toResponseDto(conversation, List.of());
    }

    @Transactional(readOnly = true)
    public Page<ConversationResponseDto> getAllConversations(Long userId, Pageable pageable) {
        Page<ConversationEntity> page = conversationRepository.findByUserIdOrderByStartedAtDesc(userId, pageable);
        return page.map(conversation -> conversationConverter.toResponseDto(conversation, List.of()));
    }

    @Transactional(readOnly = true)
    public ConversationResponseDto getConversationById(Long userId, String conversationId) {
        ConversationEntity conversation = findOwnedConversation(userId, conversationId);
        List<ConversationMessageEntity> messages =
                conversationMessageRepository.findByConversationIdOrderByCreatedDateAsc(conversationId);
        return conversationConverter.toResponseDto(conversation, messages);
    }

    /**
     * Persists the user's message, calls the Coach for a reply, persists that too, and returns
     * just the assistant's reply (the caller already has the user's own message optimistically).
     */
    public ConversationMessageResponseDto sendMessage(Long userId, String conversationId, String content) {
        ConversationEntity conversation = findOwnedConversation(userId, conversationId);
        if (conversation.getStatus() != ConversationStatus.ACTIVE) {
            throw new ValidationException("Conversation is not active");
        }

        List<ConversationMessageEntity> history =
                conversationMessageRepository.findByConversationIdOrderByCreatedDateAsc(conversationId);

        ConversationMessageEntity userMessage = new ConversationMessageEntity(
                UUID.randomUUID().toString(), conversation, MessageRole.USER, content);
        conversationMessageRepository.save(userMessage);

        String replyText = coachAgentService.getReply(history, content, conversation.getUser().getCoreValues());

        ConversationMessageEntity assistantMessage = new ConversationMessageEntity(
                UUID.randomUUID().toString(), conversation, MessageRole.ASSISTANT, replyText);
        conversationMessageRepository.save(assistantMessage);

        return conversationConverter.toMessageResponseDto(assistantMessage);
    }

    /**
     * Ends the session and publishes an event that triggers background memory extraction once
     * this transaction commits (see MemoryExtractionService.onConversationEnded) — not called
     * directly, to avoid the extraction reading pre-commit state.
     */
    public ConversationResponseDto endConversation(Long userId, String conversationId) {
        ConversationEntity conversation = findOwnedConversation(userId, conversationId);
        if (conversation.getStatus() == ConversationStatus.ACTIVE) {
            conversation.end();
            conversationRepository.save(conversation);
            eventPublisher.publishEvent(new ConversationEndedEvent(conversationId));
        }
        List<ConversationMessageEntity> messages =
                conversationMessageRepository.findByConversationIdOrderByCreatedDateAsc(conversationId);
        return conversationConverter.toResponseDto(conversation, messages);
    }

    /**
     * Generates (and persists) a human-readable markdown recap of the conversation so far.
     * Callable any time — while ACTIVE or after ENDED — and safe to call again to regenerate.
     */
    public ConversationResponseDto summarizeConversation(Long userId, String conversationId) {
        ConversationEntity conversation = findOwnedConversation(userId, conversationId);
        List<ConversationMessageEntity> messages =
                conversationMessageRepository.findByConversationIdOrderByCreatedDateAsc(conversationId);

        String summary = conversationSummaryService.summarize(messages);
        conversation.setSummary(summary);
        conversationRepository.save(conversation);

        return conversationConverter.toResponseDto(conversation, messages);
    }

    private ConversationEntity findOwnedConversation(Long userId, String conversationId) {
        return conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
    }
}
