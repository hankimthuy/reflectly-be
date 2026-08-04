package org.mentorship.reflectly.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiClientConfig {

    /**
     * Blank api-key is tolerated at startup (local dev without GEMINI_API_KEY yet) — the
     * SDK only fails on the first actual request, not on client construction.
     */
    @Bean
    public Client geminiClient(@Value("${app.gemini.api-key:}") String apiKey) {
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }
}
