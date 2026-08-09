package org.mentorship.reflectly.ai;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mentorship.reflectly.constants.AiConstants;
import org.mentorship.reflectly.model.ConversationMessageEntity;
import org.mentorship.reflectly.model.MessageRole;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Real-time Socratic Coach — reflects the user's own words back, asks open questions, never
 * concludes or diagnoses for them. Not a therapy/crisis-intervention tool: on distress signals
 * it acknowledges concern and points toward professional support, nothing more.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoachAgentService {

    private static final String SYSTEM_PROMPT = """
            Bạn là một AI Coach trong ứng dụng lãnh đạo bản thân, đóng vai trò người đồng hành \
            khai vấn theo phong cách Socratic (Socratic coaching).

            Nguyên tắc bắt buộc:
            1. Đặt câu hỏi mở để giúp người dùng tự đào sâu suy nghĩ, cảm xúc và giá trị của họ — \
            không đưa ra kết luận thay họ, không phán xét đúng/sai.
            2. Phản chiếu lại (reflect) điều người dùng vừa chia sẻ bằng ngôn ngữ của chính họ, \
            trước khi hỏi câu tiếp theo, để họ cảm thấy được lắng nghe thật sự.
            3. Không chẩn đoán tâm lý, không đưa ra lời khuyên y tế/trị liệu. Đây không phải công \
            cụ trị liệu.
            4. Nếu người dùng nhắc đến tên người khác, sự kiện, hoặc mối quan hệ cụ thể, hãy khai \
            thác thêm một chút về cảm xúc và bối cảnh — nhưng đừng biến cuộc trò chuyện thành thẩm vấn.
            5. Nếu phát hiện dấu hiệu khủng hoảng tâm lý nghiêm trọng (ý định tự hại, tuyệt vọng cùng \
            cực...), hãy phản hồi với sự quan tâm chân thành, khuyến khích họ tìm đến chuyên gia hoặc \
            đường dây hỗ trợ tâm lý phù hợp — không cố gắng "xử lý" tình huống đó một mình.
            6. Giữ giọng điệu ấm áp, ngắn gọn, tôn trọng — mỗi lượt trả lời thường 2-5 câu, không \
            giảng giải dài dòng.
            7. Luôn trả lời bằng tiếng Việt.
            """;

    /** Mirrors the onboarding.value.* labels in reflectly-fe/src/i18n/locales/vi.json. */
    private static final Map<String, String> CORE_VALUE_LABELS = Map.ofEntries(
            Map.entry("honesty", "Trung thực"), Map.entry("family", "Gia đình"),
            Map.entry("freedom", "Tự do"), Map.entry("creativity", "Sáng tạo"),
            Map.entry("connection", "Kết nối"), Map.entry("growth", "Trưởng thành"),
            Map.entry("balance", "Cân bằng"), Map.entry("courage", "Can đảm"),
            Map.entry("peace", "Bình an"), Map.entry("contribution", "Cống hiến"),
            Map.entry("achievement", "Thành tựu"), Map.entry("learning", "Học hỏi"));

    private final Client geminiClient;

    /**
     * @param history         prior turns in this conversation, oldest first. Entries with a
     *                         purged (null) content are skipped.
     * @param newUserMessage  the user's latest message, appended after history.
     * @param coreValues      user's self-selected core values (onboarding keys, e.g. "honesty"),
     *                        possibly empty if they skipped that step.
     * @return the Coach's reply text.
     */
    public String getReply(List<ConversationMessageEntity> history, String newUserMessage, List<String> coreValues) {
        List<Content> contents = new ArrayList<>();
        for (ConversationMessageEntity message : history) {
            if (message.getContent() == null) {
                continue;
            }
            String role = message.getRole() == MessageRole.USER ? "user" : "model";
            contents.add(Content.builder().role(role).parts(Part.fromText(message.getContent())).build());
        }
        contents.add(Content.builder().role("user").parts(Part.fromText(newUserMessage)).build());

        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(Content.builder().parts(Part.fromText(buildSystemPrompt(coreValues))).build())
                .maxOutputTokens(AiConstants.COACH_MAX_OUTPUT_TOKENS)
                .build();

        GenerateContentResponse response = geminiClient.models.generateContent(AiConstants.COACH_MODEL, contents, config);
        logTokenUsage("coach reply", response);
        return response.text();
    }

    /** Best-effort spend visibility in Azure logs — no dashboard, just something to grep. */
    private void logTokenUsage(String callSite, GenerateContentResponse response) {
        response.usageMetadata().ifPresent(usage -> log.info(
                "Gemini tokens used ({}): total={}, prompt={}, candidates={}",
                callSite,
                usage.totalTokenCount().orElse(null),
                usage.promptTokenCount().orElse(null),
                usage.candidatesTokenCount().orElse(null)));
    }

    private String buildSystemPrompt(List<String> coreValues) {
        if (coreValues == null || coreValues.isEmpty()) {
            return SYSTEM_PROMPT;
        }
        String labels = coreValues.stream()
                .map(key -> CORE_VALUE_LABELS.getOrDefault(key, key))
                .collect(Collectors.joining(", "));
        return SYSTEM_PROMPT + "\n\nBối cảnh về người dùng: họ đã tự chọn những giá trị cốt lõi sau cho bản thân — "
                + labels + ". Khi phù hợp, hãy phản chiếu câu hỏi và quan sát của bạn theo hướng những giá trị "
                + "này (không cần nhắc lại danh sách này với họ, chỉ dùng nó để hiểu bối cảnh).";
    }
}
