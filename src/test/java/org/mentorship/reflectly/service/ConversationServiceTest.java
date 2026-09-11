package org.mentorship.reflectly.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mentorship.reflectly.ai.CoachAgentService;
import org.mentorship.reflectly.ai.ConversationSummaryService;
import org.mentorship.reflectly.converter.ConversationConverter;
import org.mentorship.reflectly.dto.ConversationMessageResponseDto;
import org.mentorship.reflectly.dto.ConversationResponseDto;
import org.mentorship.reflectly.dto.SendMessageResponseDto;
import org.mentorship.reflectly.model.ConversationEntity;
import org.mentorship.reflectly.model.ConversationMessageEntity;
import org.mentorship.reflectly.model.Emotion;
import org.mentorship.reflectly.model.MessageRole;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.exception.QuotaExceededException;
import org.mentorship.reflectly.repository.ConversationMessageRepository;
import org.mentorship.reflectly.repository.ConversationRepository;
import org.mentorship.reflectly.repository.UserRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the alpha-phase conversation quota added to bound Gemini API spend (see
 * app.quota.max-conversations-per-user) and the mood readings persisted alongside chat messages.
 */
@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationMessageRepository conversationMessageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ConversationConverter conversationConverter;
    @Mock
    private CoachAgentService coachAgentService;
    @Mock
    private ConversationSummaryService conversationSummaryService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ConversationService conversationService;

    /** Real instance, not a mock: it is stateless, side-effect free, and the thing under test here. */
    private final MoodScoringService moodScoringService = new MoodScoringService();

    @BeforeEach
    void setUp() {
        conversationService = new ConversationService(
                conversationRepository, conversationMessageRepository, userRepository,
                conversationConverter, coachAgentService, conversationSummaryService,
                moodScoringService, eventPublisher);
        ReflectionTestUtils.setField(conversationService, "maxConversationsPerUser", 5L);
    }

    @Test
    void startConversation_throwsWhenAtQuota() {
        Long userId = 42L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(new UserEntity()));
        when(conversationRepository.countByUserId(userId)).thenReturn(5L);

        assertThrows(QuotaExceededException.class, () -> conversationService.startConversation(userId));

        verify(conversationRepository, never()).save(any());
    }

    @Test
    void startConversation_succeedsUnderQuota() {
        Long userId = 42L;
        UserEntity user = new UserEntity();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(conversationRepository.countByUserId(userId)).thenReturn(4L);
        when(conversationConverter.toResponseDto(any(ConversationEntity.class), eq(List.of())))
                .thenReturn(ConversationResponseDto.builder().build());

        ConversationResponseDto result = conversationService.startConversation(userId);

        assertThat(result).isNotNull();
        verify(conversationRepository).save(any(ConversationEntity.class));
    }

    @Test
    void sendMessage_setsMoodFieldsOnUserMessageWhenHeuristicMatches() {
        ConversationEntity conversation = activeConversation();
        stubSendMessageCollaborators(conversation);

        SendMessageResponseDto result = conversationService.sendMessage(
                USER_ID, CONVERSATION_ID, "I feel so anxious about the review tomorrow");

        assertThat(result).isNotNull();
        ConversationMessageEntity savedUserMessage = captureSavedMessage(MessageRole.USER);
        assertThat(savedUserMessage.getMoodEmotion()).isEqualTo(Emotion.ANXIOUS);
        assertThat(savedUserMessage.getMoodScore()).isEqualTo(0.95);
    }

    @Test
    void sendMessage_leavesMoodFieldsNullWhenNothingMatches() {
        ConversationEntity conversation = activeConversation();
        stubSendMessageCollaborators(conversation);

        conversationService.sendMessage(USER_ID, CONVERSATION_ID, "The report is attached to the email.");

        ConversationMessageEntity savedUserMessage = captureSavedMessage(MessageRole.USER);
        assertThat(savedUserMessage.getMoodEmotion()).isNull();
        assertThat(savedUserMessage.getMoodScore()).isNull();
    }

    @Test
    void sendMessage_returnsBothPersistedUserMessageAndAssistantReply() {
        ConversationEntity conversation = activeConversation();
        stubSendMessageCollaborators(conversation);

        SendMessageResponseDto result = conversationService.sendMessage(
                USER_ID, CONVERSATION_ID, "feeling good today");

        assertThat(result.getUserMessage()).isNotNull();
        assertThat(result.getAssistantMessage()).isNotNull();
    }

    @Test
    void endConversation_computesMoodArcFromFirstAndLastScoredUserMessages() {
        ConversationEntity conversation = activeConversation();
        when(conversationRepository.findByIdAndUserId(CONVERSATION_ID, USER_ID))
                .thenReturn(Optional.of(conversation));
        when(conversationMessageRepository.findByConversationIdOrderByCreatedDateAsc(CONVERSATION_ID))
                .thenReturn(List.of(
                        scoredMessage(conversation, MessageRole.USER, Emotion.ANXIOUS, 0.95),
                        // An assistant message in between must not be mistaken for the user's arc.
                        scoredMessage(conversation, MessageRole.ASSISTANT, Emotion.ANGRY, 0.9),
                        unscoredMessage(conversation, MessageRole.USER),
                        scoredMessage(conversation, MessageRole.USER, Emotion.GOOD, 0.25)));

        conversationService.endConversation(USER_ID, CONVERSATION_ID);

        assertThat(conversation.getInitialMoodEmotion()).isEqualTo(Emotion.ANXIOUS);
        assertThat(conversation.getInitialMoodScore()).isEqualTo(0.95);
        assertThat(conversation.getFinalMoodEmotion()).isEqualTo(Emotion.GOOD);
        assertThat(conversation.getFinalMoodScore()).isEqualTo(0.25);
        verify(conversationRepository).save(conversation);
    }

    @Test
    void endConversation_leavesMoodArcNullWhenNoMessageWasScored() {
        ConversationEntity conversation = activeConversation();
        when(conversationRepository.findByIdAndUserId(CONVERSATION_ID, USER_ID))
                .thenReturn(Optional.of(conversation));
        when(conversationMessageRepository.findByConversationIdOrderByCreatedDateAsc(CONVERSATION_ID))
                .thenReturn(List.of(
                        unscoredMessage(conversation, MessageRole.USER),
                        unscoredMessage(conversation, MessageRole.ASSISTANT)));

        conversationService.endConversation(USER_ID, CONVERSATION_ID);

        assertThat(conversation.getInitialMoodEmotion()).isNull();
        assertThat(conversation.getInitialMoodScore()).isNull();
        assertThat(conversation.getFinalMoodEmotion()).isNull();
        assertThat(conversation.getFinalMoodScore()).isNull();
    }

    @Test
    void endConversation_onAlreadyEndedSessionDoesNotRecomputeMoodArc() {
        ConversationEntity conversation = activeConversation();
        conversation.end();
        conversation.setInitialMoodEmotion(Emotion.ANXIOUS);
        conversation.setInitialMoodScore(0.95);
        conversation.setFinalMoodEmotion(Emotion.HAPPY);
        conversation.setFinalMoodScore(0.1);

        when(conversationRepository.findByIdAndUserId(CONVERSATION_ID, USER_ID))
                .thenReturn(Optional.of(conversation));
        when(conversationMessageRepository.findByConversationIdOrderByCreatedDateAsc(CONVERSATION_ID))
                .thenReturn(List.of(scoredMessage(conversation, MessageRole.USER, Emotion.ANGRY, 0.9)));

        conversationService.endConversation(USER_ID, CONVERSATION_ID);

        // Untouched — the ACTIVE-only guard keeps ending a session idempotent.
        assertThat(conversation.getInitialMoodEmotion()).isEqualTo(Emotion.ANXIOUS);
        assertThat(conversation.getFinalMoodEmotion()).isEqualTo(Emotion.HAPPY);
        assertThat(conversation.getFinalMoodScore()).isEqualTo(0.1);
        verify(conversationRepository, never()).save(any(ConversationEntity.class));
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    private static final Long USER_ID = 42L;
    private static final String CONVERSATION_ID = "conversation-1";

    private ConversationEntity activeConversation() {
        UserEntity user = new UserEntity();
        user.setId(USER_ID);
        return new ConversationEntity(CONVERSATION_ID, user);
    }

    private void stubSendMessageCollaborators(ConversationEntity conversation) {
        when(conversationRepository.findByIdAndUserId(CONVERSATION_ID, USER_ID))
                .thenReturn(Optional.of(conversation));
        when(conversationMessageRepository.findByConversationIdOrderByCreatedDateAsc(CONVERSATION_ID))
                .thenReturn(List.of());
        when(coachAgentService.getReply(anyList(), anyString(), any()))
                .thenReturn("What makes you say that?");
        when(conversationConverter.toMessageResponseDto(any(ConversationMessageEntity.class)))
                .thenReturn(ConversationMessageResponseDto.builder().build());
    }

    private ConversationMessageEntity captureSavedMessage(MessageRole role) {
        ArgumentCaptor<ConversationMessageEntity> captor =
                ArgumentCaptor.forClass(ConversationMessageEntity.class);
        verify(conversationMessageRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        return captor.getAllValues().stream()
                .filter(message -> message.getRole() == role)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No persisted message with role " + role));
    }

    private ConversationMessageEntity scoredMessage(ConversationEntity conversation, MessageRole role,
                                                    Emotion emotion, double score) {
        ConversationMessageEntity message = unscoredMessage(conversation, role);
        message.setMoodEmotion(emotion);
        message.setMoodScore(score);
        return message;
    }

    private ConversationMessageEntity unscoredMessage(ConversationEntity conversation, MessageRole role) {
        return new ConversationMessageEntity(
                java.util.UUID.randomUUID().toString(), conversation, role, "text");
    }
}
