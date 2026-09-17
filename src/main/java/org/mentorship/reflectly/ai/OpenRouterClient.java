package org.mentorship.reflectly.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin client over OpenRouter's OpenAI-compatible chat-completions endpoint — the single place
 * the backend talks to a model, used by {@link CoachAgentService},
 * {@link ConversationSummaryService} and {@link MemoryExtractionService}.
 *
 * <p>Deliberately non-streaming: the API returns one finished reply per call (nothing downstream
 * consumes tokens as they arrive), and a non-streamed response still carries the {@code usage}
 * block this logs for spend visibility.
 */
@Slf4j
@Component
public class OpenRouterClient {

    /** One turn in the conversation sent to the model. Role is "system", "user" or "assistant". */
    public record ChatMessage(String role, String content) {

        public static ChatMessage system(String content) {
            return new ChatMessage("system", content);
        }

        public static ChatMessage user(String content) {
            return new ChatMessage("user", content);
        }
    }

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final boolean configured;

    public OpenRouterClient(
            ObjectMapper objectMapper,
            @Value("${app.openrouter.api-key:}") String apiKey,
            @Value("${app.openrouter.base-url:https://openrouter.ai/api/v1}") String baseUrl,
            @Value("${app.openrouter.timeout-seconds:60}") int timeoutSeconds,
            @Value("${app.openrouter.site-url:}") String siteUrl,
            @Value("${app.openrouter.site-name:Reflectly}") String siteName) {
        this.objectMapper = objectMapper;
        this.configured = apiKey != null && !apiKey.isBlank();

        // A blank key is tolerated at startup (local dev without OPENROUTER_API_KEY yet) and only
        // fails on the first actual request — same behaviour the Gemini client had, so the app
        // still boots for everything that isn't the Coach.
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + (apiKey == null ? "" : apiKey))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        // Optional attribution headers OpenRouter uses for its app leaderboard — harmless if unset.
        if (siteUrl != null && !siteUrl.isBlank()) {
            builder = builder.defaultHeader("HTTP-Referer", siteUrl);
        }
        if (siteName != null && !siteName.isBlank()) {
            builder = builder.defaultHeader("X-Title", siteName);
        }
        this.restClient = builder.build();
    }

    /**
     * @param model     an OpenRouter model slug (see {@link org.mentorship.reflectly.constants.AiConstants}).
     * @param messages  the full prompt, system message first if there is one.
     * @param maxTokens cap on the reply length.
     * @param jsonOnly  when true, asks the model for a JSON object response
     *                  ({@code response_format: json_object}) instead of prose.
     * @param callSite  short label used only in the token-usage log line.
     * @return the assistant's reply text.
     */
    public String complete(String model, List<ChatMessage> messages, int maxTokens, boolean jsonOnly, String callSite) {
        if (!configured) {
            throw new IllegalStateException("OPENROUTER_API_KEY is not configured — the AI Coach cannot answer.");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("max_tokens", maxTokens);
        body.put("messages", messages.stream()
                .map(message -> Map.of("role", message.role(), "content", message.content()))
                .toList());
        if (jsonOnly) {
            body.put("response_format", Map.of("type", "json_object"));
        }

        JsonNode response;
        try {
            response = restClient.post()
                    .uri("/chat/completions")
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            // Never echo the request (it carries the transcript) or the key — just what failed.
            throw new IllegalStateException("OpenRouter request failed (" + callSite + "): " + e.getMessage(), e);
        }

        if (response == null) {
            throw new IllegalStateException("OpenRouter returned an empty response (" + callSite + ")");
        }
        if (response.hasNonNull("error")) {
            throw new IllegalStateException(
                    "OpenRouter returned an error (" + callSite + "): " + response.get("error").path("message").asText());
        }

        logTokenUsage(callSite, response);

        String content = response.path("choices").path(0).path("message").path("content").asText(null);
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("OpenRouter returned no content (" + callSite + ")");
        }
        return content;
    }

    /** Convenience for a single-prompt call with an optional system instruction. */
    public String complete(String model, String systemPrompt, String userPrompt, int maxTokens, boolean jsonOnly,
                           String callSite) {
        List<ChatMessage> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(ChatMessage.system(systemPrompt));
        }
        messages.add(ChatMessage.user(userPrompt));
        return complete(model, messages, maxTokens, jsonOnly, callSite);
    }

    /**
     * Strips a markdown code fence if the model wrapped its JSON in one — {@code json_object}
     * mode makes that rare but not impossible, and a fence is the difference between a parsed
     * extraction and a discarded one.
     */
    public JsonNode parseJson(String content, String callSite) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int closingFence = trimmed.lastIndexOf("```");
            if (firstNewline > 0 && closingFence > firstNewline) {
                trimmed = trimmed.substring(firstNewline + 1, closingFence).trim();
            }
        }
        try {
            return objectMapper.readTree(trimmed);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse OpenRouter response as JSON (" + callSite + ")", e);
        }
    }

    /** Best-effort spend visibility in Azure logs — no dashboard, just something to grep. */
    private void logTokenUsage(String callSite, JsonNode response) {
        JsonNode usage = response.path("usage");
        if (usage.isMissingNode() || usage.isNull()) {
            return;
        }
        log.info("OpenRouter tokens used ({}): total={}, prompt={}, completion={}",
                callSite,
                usage.path("total_tokens").asText("?"),
                usage.path("prompt_tokens").asText("?"),
                usage.path("completion_tokens").asText("?"));
    }
}
