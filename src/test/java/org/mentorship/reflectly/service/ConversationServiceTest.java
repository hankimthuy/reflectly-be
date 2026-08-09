package org.mentorship.reflectly.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mentorship.reflectly.ai.CoachAgentService;
import org.mentorship.reflectly.ai.ConversationSummaryService;
import org.mentorship.reflectly.converter.ConversationConverter;
import org.mentorship.reflectly.dto.ConversationResponseDto;
import org.mentorship.reflectly.exception.QuotaExceededException;
import org.mentorship.reflectly.model.ConversationEntity;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.repository.ConversationMessageRepository;
import org.mentorship.reflectly.repository.ConversationRepository;
import org.mentorship.reflectly.repository.UserRepository;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the alpha-phase conversation quota added to bound Gemini API spend — see
 * app.quota.max-conversations-per-user.
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

    @BeforeEach
    void setUp() {
        conversationService = new ConversationService(
                conversationRepository, conversationMessageRepository, userRepository,
                conversationConverter, coachAgentService, conversationSummaryService, eventPublisher);
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
}
