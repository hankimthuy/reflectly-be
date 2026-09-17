package org.mentorship.reflectly.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mentorship.reflectly.constants.AiConstants;
import org.mentorship.reflectly.model.ConversationMessageEntity;
import org.mentorship.reflectly.model.MessageRole;
import org.mentorship.reflectly.repository.ConversationMessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * Runs after a Coach session ends: turns the raw transcript into structured People/
 * RelationshipEvents/Insights via a cheap model on OpenRouter, then (by default) purges the raw
 * message content — only the derived, structured data is meant to persist long-term.
 * <p>
 * Orchestration only — all DB writes go through {@link MemoryExtractionPersister} so
 * @Transactional actually applies (see its class doc for why that split matters).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryExtractionService {

    /**
     * OpenRouter's OpenAI-compatible API has no equivalent of Gemini's typed responseSchema, so
     * the shape is stated in the prompt and the call is made in JSON-object mode. Jackson ignores
     * unknown fields (see ExtractionResult), and a missing or malformed answer fails the
     * extraction the same way a schema violation used to.
     */
    private static final String SCHEMA_SPEC = """
            {
              "people": [
                { "name": "string", "relationshipType": "FAMILY|FRIEND|PARTNER|COLLEAGUE|MANAGER|OTHER" }
              ],
              "events": [
                { "personName": "string (phải trùng một name trong people)",
                  "eventType": "CONFLICT|BONDING|NEUTRAL",
                  "summary": "string",
                  "sentimentScore": "số từ -1 đến 1" }
              ],
              "insights": [
                { "insightText": "string",
                  "category": "VALUE|BEHAVIOR_PATTERN|RELATIONSHIP",
                  "personName": "string hoặc null" }
              ]
            }
            """;

    private final OpenRouterClient openRouterClient;
    private final ObjectMapper objectMapper;
    private final ConversationMessageRepository conversationMessageRepository;
    private final MemoryExtractionPersister persister;

    @Value("${app.conversations.purge-after-extraction:true}")
    private boolean purgeAfterExtraction;

    /**
     * Fires only after the ending transaction actually commits (AFTER_COMMIT) — starting the
     * async extraction any earlier risks a race where this reads the conversation/messages
     * before their final state is visible.
     */
    @Async("memoryExtractionExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onConversationEnded(ConversationEndedEvent event) {
        String conversationId = event.conversationId();
        try {
            List<ConversationMessageEntity> messages =
                    conversationMessageRepository.findByConversationIdOrderByCreatedDateAsc(conversationId);
            String transcript = buildTranscript(messages);

            if (transcript.isBlank()) {
                persister.markExtracted(conversationId);
                return;
            }

            ExtractionResult result = callExtractionModel(transcript);
            persister.applyExtraction(conversationId, result, purgeAfterExtraction);
        } catch (Exception e) {
            log.error("Memory extraction failed for conversation {}", conversationId, e);
            persister.markFailed(conversationId);
        }
    }

    private String buildTranscript(List<ConversationMessageEntity> messages) {
        StringBuilder sb = new StringBuilder();
        for (ConversationMessageEntity message : messages) {
            if (message.getContent() == null) {
                continue;
            }
            String speaker = message.getRole() == MessageRole.USER ? "Người dùng" : "Coach";
            sb.append(speaker).append(": ").append(message.getContent()).append("\n");
        }
        return sb.toString();
    }

    private ExtractionResult callExtractionModel(String transcript) {
        String prompt = """
                Đọc đoạn hội thoại giữa Người dùng và Coach dưới đây. Trích xuất, theo đúng schema JSON:
                - people: những người cụ thể (không phải "sếp" chung chung trừ khi không có tên) mà Người dùng \
                nhắc đến, kèm loại quan hệ ước lượng hợp lý nhất.
                - events: các sự kiện/tương tác liên quan đến từng người ở trên, có tóm tắt ngắn gọn và \
                điểm cảm xúc (-1 tiêu cực đến 1 tích cực).
                - insights: nhận định ngắn gọn về giá trị cốt lõi, mẫu hành vi, hoặc mối quan hệ mà Người dùng \
                bộc lộ qua cuộc trò chuyện — không suy diễn quá xa những gì thực sự được nói. Nếu insight \
                gắn liền với một người cụ thể đã liệt kê ở "people" (thường là loại RELATIONSHIP), điền tên \
                người đó vào personName; nếu không, để personName là null.
                Nếu không có thông tin phù hợp cho một mục, trả về mảng rỗng cho mục đó. Chỉ trích xuất những \
                gì thực sự xuất hiện trong hội thoại, không bịa thêm.

                Chỉ trả về một object JSON duy nhất, không kèm giải thích, theo đúng cấu trúc sau:
                %s

                Hội thoại:
                %s
                """.formatted(SCHEMA_SPEC, transcript);

        String content = openRouterClient.complete(
                AiConstants.MEMORY_EXTRACTION_MODEL,
                null,
                prompt,
                AiConstants.EXTRACTION_MAX_OUTPUT_TOKENS,
                true,
                "memory extraction");

        try {
            return objectMapper.treeToValue(
                    openRouterClient.parseJson(content, "memory extraction"), ExtractionResult.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to map extraction model response onto ExtractionResult", e);
        }
    }
}
