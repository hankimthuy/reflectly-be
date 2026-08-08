package org.mentorship.reflectly.ai;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.constants.AiConstants;
import org.mentorship.reflectly.model.ConversationMessageEntity;
import org.mentorship.reflectly.model.MessageRole;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * On-demand, human-readable recap of a Coach conversation — distinct from
 * {@link MemoryExtractionService}, which produces structured People/Events/Insights for
 * background storage. This produces markdown prose meant to be shown directly to the user in
 * the chat UI (e.g. after they ask to "tóm tắt" the session), and to seed the Insight Catcher
 * save flow.
 */
@Service
@RequiredArgsConstructor
public class ConversationSummaryService {

    private static final String PROMPT_TEMPLATE = """
            Đọc đoạn hội thoại giữa Người dùng và Coach dưới đây. Viết một bản tóm tắt ngắn gọn, \
            súc tích bằng tiếng Việt dưới dạng markdown (dùng gạch đầu dòng), nêu bật:
            - Những insight hoặc nhận thức quan trọng người dùng đã tự đúc kết được.
            - Các giá trị, mục tiêu, hoặc mối bận tâm cốt lõi được nhắc đến.
            - Nếu có, các bước hành động hoặc điều người dùng muốn ghi nhớ.

            Chỉ tóm tắt những gì thực sự xuất hiện trong hội thoại, không suy diễn hay bịa thêm. \
            Giữ giọng điệu ấm áp, khách quan, không phán xét.

            Hội thoại:
            %s
            """;

    private final Client geminiClient;

    public String summarize(List<ConversationMessageEntity> messages) {
        String transcript = buildTranscript(messages);
        if (transcript.isBlank()) {
            return "Chưa có nội dung trò chuyện để tóm tắt.";
        }

        GenerateContentConfig config = GenerateContentConfig.builder()
                .maxOutputTokens(AiConstants.EXTRACTION_MAX_OUTPUT_TOKENS)
                .build();

        GenerateContentResponse response = geminiClient.models.generateContent(
                AiConstants.MEMORY_EXTRACTION_MODEL,
                Content.builder().role("user").parts(Part.fromText(PROMPT_TEMPLATE.formatted(transcript))).build(),
                config);

        return response.text();
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
}
