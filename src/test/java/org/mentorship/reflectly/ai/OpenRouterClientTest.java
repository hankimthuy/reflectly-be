package org.mentorship.reflectly.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mentorship.reflectly.ai.OpenRouterClient.ChatMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Covers the one place the backend talks to a model. Runs against a local stub of OpenRouter's
 * chat-completions endpoint rather than the real service — no network, no API key, no spend.
 */
class OpenRouterClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpServer server;
    private String baseUrl;
    private final AtomicReference<String> lastRequestBody = new AtomicReference<>();
    private final AtomicReference<String> lastAuthHeader = new AtomicReference<>();
    private volatile String responseBody;

    @BeforeEach
    void startStub() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/chat/completions", exchange -> {
            lastRequestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            lastAuthHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopStub() {
        server.stop(0);
    }

    private OpenRouterClient clientWithKey(String apiKey) {
        return new OpenRouterClient(objectMapper, apiKey, baseUrl, 10, "", "Reflectly");
    }

    @Test
    void sendsTheModelAndMessagesAndReturnsTheReplyText() {
        responseBody = """
                {"choices":[{"message":{"role":"assistant","content":"Bạn thấy thế nào về điều đó?"}}],
                 "usage":{"prompt_tokens":10,"completion_tokens":5,"total_tokens":15}}
                """;

        String reply = clientWithKey("sk-test").complete(
                "some/model",
                List.of(ChatMessage.system("bạn là coach"), ChatMessage.user("chào")),
                256,
                false,
                "test");

        assertThat(reply).isEqualTo("Bạn thấy thế nào về điều đó?");
        assertThat(lastAuthHeader.get()).isEqualTo("Bearer sk-test");
        assertThat(lastRequestBody.get())
                .contains("\"model\":\"some/model\"")
                .contains("\"max_tokens\":256")
                .contains("\"role\":\"system\"")
                .contains("\"role\":\"user\"")
                .doesNotContain("response_format");
    }

    @Test
    void asksForAJsonObjectWhenJsonOnly() {
        responseBody = """
                {"choices":[{"message":{"content":"{\\"people\\":[]}"}}]}
                """;

        String reply = clientWithKey("sk-test")
                .complete("some/model", null, "trích xuất đi", 128, true, "test");

        assertThat(lastRequestBody.get()).contains("\"response_format\":{\"type\":\"json_object\"}");
        assertThat(reply).isEqualTo("{\"people\":[]}");
    }

    @Test
    void parsesJsonEvenWhenTheModelWrapsItInACodeFence() {
        OpenRouterClient client = clientWithKey("sk-test");

        assertThat(client.parseJson("```json\n{\"people\":[{\"name\":\"Minh\"}]}\n```", "test")
                .path("people").path(0).path("name").asText())
                .isEqualTo("Minh");
    }

    @Test
    void surfacesAnErrorPayloadInsteadOfReturningEmptyContent() {
        responseBody = """
                {"error":{"message":"No endpoints found for this model"}}
                """;

        assertThatThrownBy(() -> clientWithKey("sk-test")
                .complete("some/model", null, "chào", 128, false, "test"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No endpoints found for this model");
    }

    @Test
    void failsClearlyWhenNoApiKeyIsConfigured() {
        responseBody = "{}";

        assertThatThrownBy(() -> clientWithKey("")
                .complete("some/model", null, "chào", 128, false, "test"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OPENROUTER_API_KEY");
    }
}
