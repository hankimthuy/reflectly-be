package org.mentorship.reflectly.ai;

/** Published after a conversation's "ended" state has actually committed — see MemoryExtractionService. */
public record ConversationEndedEvent(String conversationId) {
}
