package org.mentorship.reflectly.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
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
import java.util.Map;

/**
 * Runs after a Coach session ends: turns the raw transcript into structured People/
 * RelationshipEvents/Insights via a cheap Gemini model, then (by default) purges the raw
 * message content — only the derived, structured data is meant to persist long-term.
 * <p>
 * Orchestration only — all DB writes go through {@link MemoryExtractionPersister} so
 * @Transactional actually applies (see its class doc for why that split matters).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryExtractionService {

    private final Client geminiClient;
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

                Hội thoại:
                %s
                """.formatted(transcript);

        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .responseSchema(buildResponseSchema())
                .maxOutputTokens(AiConstants.EXTRACTION_MAX_OUTPUT_TOKENS)
                .build();

        GenerateContentResponse response = geminiClient.models.generateContent(
                AiConstants.MEMORY_EXTRACTION_MODEL,
                Content.builder().role("user").parts(Part.fromText(prompt)).build(),
                config);

        response.usageMetadata().ifPresent(usage -> log.info(
                "Gemini tokens used (memory extraction): total={}, prompt={}, candidates={}",
                usage.totalTokenCount().orElse(null),
                usage.promptTokenCount().orElse(null),
                usage.candidatesTokenCount().orElse(null)));

        try {
            return objectMapper.readValue(response.text(), ExtractionResult.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse extraction model response as JSON", e);
        }
    }

    private Schema buildResponseSchema() {
        Schema personSchema = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of(
                        "name", Schema.builder().type(Type.Known.STRING).build(),
                        "relationshipType", Schema.builder().type(Type.Known.STRING)
                                .enum_("FAMILY", "FRIEND", "PARTNER", "COLLEAGUE", "MANAGER", "OTHER").build()))
                .required("name", "relationshipType")
                .build();

        Schema eventSchema = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of(
                        "personName", Schema.builder().type(Type.Known.STRING).build(),
                        "eventType", Schema.builder().type(Type.Known.STRING)
                                .enum_("CONFLICT", "BONDING", "NEUTRAL").build(),
                        "summary", Schema.builder().type(Type.Known.STRING).build(),
                        "sentimentScore", Schema.builder().type(Type.Known.NUMBER).build()))
                .required("personName", "eventType", "summary")
                .build();

        Schema insightSchema = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of(
                        "insightText", Schema.builder().type(Type.Known.STRING).build(),
                        "category", Schema.builder().type(Type.Known.STRING)
                                .enum_("VALUE", "BEHAVIOR_PATTERN", "RELATIONSHIP").build(),
                        "personName", Schema.builder().type(Type.Known.STRING).build()))
                .required("insightText", "category")
                .build();

        return Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of(
                        "people", Schema.builder().type(Type.Known.ARRAY).items(personSchema).build(),
                        "events", Schema.builder().type(Type.Known.ARRAY).items(eventSchema).build(),
                        "insights", Schema.builder().type(Type.Known.ARRAY).items(insightSchema).build()))
                .required("people", "events", "insights")
                .build();
    }
}
