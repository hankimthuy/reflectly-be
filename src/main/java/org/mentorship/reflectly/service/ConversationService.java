package org.mentorship.reflectly.service;

import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.ai.CoachAgentService;
import org.mentorship.reflectly.ai.ConversationEndedEvent;
import org.mentorship.reflectly.ai.ConversationSummaryService;
import org.mentorship.reflectly.converter.ConversationConverter;
import org.mentorship.reflectly.dto.ConversationResponseDto;
import org.mentorship.reflectly.dto.SendMessageResponseDto;
import org.mentorship.reflectly.exception.NotFoundException;
import org.mentorship.reflectly.exception.QuotaExceededException;
import org.mentorship.reflectly.exception.ValidationException;
import org.mentorship.reflectly.model.*;
import org.mentorship.reflectly.repository.ConversationMessageRepository;
import org.mentorship.reflectly.repository.ConversationRepository;
import org.mentorship.reflectly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
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
    private final MoodScoringService moodScoringService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Alpha-phase hard cap on conversations per user, to bound Gemini API spend before this app
     * has a real access/billing model. Env-tunable so it can be raised later with no code change.
     */
    @Value("${app.quota.max-conversations-per-user:5}")
    private long maxConversationsPerUser;

    public ConversationResponseDto startConversation(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        long existingCount = conversationRepository.countByUserId(userId);
        if (existingCount >= maxConversationsPerUser) {
            throw new QuotaExceededException(
                    "Bạn đã dùng hết " + maxConversationsPerUser + " lượt trò chuyện thử nghiệm với Aura.");
        }

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
     * Persists the user's message (with its mood reading, which only the server computes), calls
     * the Coach for a reply, persists that too, and returns both — the caller replaces its
     * optimistic copy of the user message with the persisted one to pick up the mood fields.
     */
    public SendMessageResponseDto sendMessage(Long userId, String conversationId, String content) {
        ConversationEntity conversation = findOwnedConversation(userId, conversationId);
        if (conversation.getStatus() != ConversationStatus.ACTIVE) {
            throw new ValidationException("Conversation is not active");
        }

        List<ConversationMessageEntity> history =
                conversationMessageRepository.findByConversationIdOrderByCreatedDateAsc(conversationId);

        ConversationMessageEntity userMessage = new ConversationMessageEntity(
                UUID.randomUUID().toString(), conversation, MessageRole.USER, content);
        moodScoringService.score(content).ifPresent(reading -> {
            userMessage.setMoodEmotion(reading.emotion());
            userMessage.setMoodScore(reading.heaviness());
        });
        conversationMessageRepository.save(userMessage);

        String replyText = coachAgentService.getReply(history, content, conversation.getUser().getCoreValues());

        ConversationMessageEntity assistantMessage = new ConversationMessageEntity(
                UUID.randomUUID().toString(), conversation, MessageRole.ASSISTANT, replyText);
        conversationMessageRepository.save(assistantMessage);

        return SendMessageResponseDto.builder()
                .userMessage(conversationConverter.toMessageResponseDto(userMessage))
                .assistantMessage(conversationConverter.toMessageResponseDto(assistantMessage))
                .build();
    }

    /**
     * Ends the session and publishes an event that triggers background memory extraction once
     * this transaction commits (see MemoryExtractionService.onConversationEnded) — not called
     * directly, to avoid the extraction reading pre-commit state.
     */
    public ConversationResponseDto endConversation(Long userId, String conversationId) {
        ConversationEntity conversation = findOwnedConversation(userId, conversationId);
        List<ConversationMessageEntity> messages =
                conversationMessageRepository.findByConversationIdOrderByCreatedDateAsc(conversationId);

        if (conversation.getStatus() == ConversationStatus.ACTIVE) {
            conversation.end();
            applyMoodArc(conversation, messages);
            conversationRepository.save(conversation);
            eventPublisher.publishEvent(new ConversationEndedEvent(conversationId));
        }
        return conversationConverter.toResponseDto(conversation, messages);
    }

    /**
     * Records where the session started and where it landed, from the first and last USER
     * messages that carried a mood reading. Left entirely null when no message was scored — an
     * absent arc is meaningfully different from a flat one. Only ever called from inside the
     * ACTIVE-only branch above, so re-ending an already-ended session cannot rewrite history.
     */
    private void applyMoodArc(ConversationEntity conversation, List<ConversationMessageEntity> messages) {
        List<ConversationMessageEntity> scored = messages.stream()
                .filter(message -> message.getRole() == MessageRole.USER)
                .filter(message -> message.getMoodScore() != null)
                .toList();
        if (scored.isEmpty()) {
            return;
        }
        ConversationMessageEntity first = scored.get(0);
        ConversationMessageEntity last = scored.get(scored.size() - 1);
        conversation.setInitialMoodEmotion(first.getMoodEmotion());
        conversation.setInitialMoodScore(first.getMoodScore());
        conversation.setFinalMoodEmotion(last.getMoodEmotion());
        conversation.setFinalMoodScore(last.getMoodScore());
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
