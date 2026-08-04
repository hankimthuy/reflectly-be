package org.mentorship.reflectly.ai;

import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.model.*;
import org.mentorship.reflectly.repository.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Transactional writes for a finished extraction — split out from MemoryExtractionService so
 * these run through a real Spring proxy. Calling a @Transactional method on `this` from within
 * the same class (self-invocation) silently skips the proxy and runs with no transaction at
 * all; a bulk @Modifying query like the purge below then fails with TransactionRequiredException
 * — a separate bean is the standard fix.
 */
@Component
@RequiredArgsConstructor
class MemoryExtractionPersister {

    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final PersonRepository personRepository;
    private final RelationshipEventRepository relationshipEventRepository;
    private final InsightRepository insightRepository;

    @Transactional
    void applyExtraction(String conversationId, ExtractionResult result, boolean purgeAfterExtraction) {
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalStateException("Conversation not found: " + conversationId));

        persist(conversation, result);

        if (purgeAfterExtraction) {
            conversationMessageRepository.purgeContentByConversationId(conversationId);
        }

        conversation.setStatus(ConversationStatus.EXTRACTED);
        conversationRepository.save(conversation);
    }

    @Transactional
    void markExtracted(String conversationId) {
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            conversation.setStatus(ConversationStatus.EXTRACTED);
            conversationRepository.save(conversation);
        });
    }

    @Transactional
    void markFailed(String conversationId) {
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            conversation.setStatus(ConversationStatus.EXTRACTION_FAILED);
            conversationRepository.save(conversation);
        });
    }

    private void persist(ConversationEntity conversation, ExtractionResult result) {
        UserEntity user = conversation.getUser();
        Map<String, PersonEntity> peopleByName = new HashMap<>();

        if (result.getPeople() != null) {
            for (ExtractionResult.ExtractedPerson extracted : result.getPeople()) {
                if (extracted.getName() == null || extracted.getName().isBlank()) {
                    continue;
                }
                PersonEntity person = personRepository
                        .findByUserIdAndNameIgnoreCase(user.getId(), extracted.getName())
                        .orElseGet(() -> new PersonEntity(
                                UUID.randomUUID().toString(), user, extracted.getName(),
                                parseEnum(RelationshipType.class, extracted.getRelationshipType(), RelationshipType.OTHER),
                                null));
                person.setLastMentionedAt(Instant.now());
                personRepository.save(person);
                peopleByName.put(extracted.getName().toLowerCase(), person);
            }
        }

        if (result.getEvents() != null) {
            for (ExtractionResult.ExtractedEvent extracted : result.getEvents()) {
                if (extracted.getPersonName() == null) {
                    continue;
                }
                PersonEntity person = peopleByName.get(extracted.getPersonName().toLowerCase());
                if (person == null) {
                    // Model referenced a person it didn't also list in `people` — skip rather than guess.
                    continue;
                }
                RelationshipEventEntity event = new RelationshipEventEntity(
                        UUID.randomUUID().toString(), person, conversation,
                        parseEnum(RelationshipEventType.class, extracted.getEventType(), RelationshipEventType.NEUTRAL),
                        extracted.getSummary(), extracted.getSentimentScore());
                relationshipEventRepository.save(event);
            }
        }

        if (result.getInsights() != null) {
            for (ExtractionResult.ExtractedInsight extracted : result.getInsights()) {
                if (extracted.getInsightText() == null || extracted.getInsightText().isBlank()) {
                    continue;
                }
                InsightEntity insight = new InsightEntity(
                        UUID.randomUUID().toString(), user, conversation,
                        extracted.getInsightText(),
                        parseEnum(InsightCategory.class, extracted.getCategory(), InsightCategory.BEHAVIOR_PATTERN));
                insightRepository.save(insight);
            }
        }
    }

    private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value, T fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
