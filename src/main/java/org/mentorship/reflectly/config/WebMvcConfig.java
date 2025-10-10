package org.mentorship.reflectly.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Base WebMvcConfigurer configuration class for common web settings
 * This class handles CORS configuration for all environments
 * Development: Spring Boot default + CORS + Vite dev server
 * Production: Spring Boot default + CORS + SPA routing fallback
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Configure CORS (Cross-Origin Resource Sharing) settings
     * This method defines which origins, methods, and headers are allowed for cross-origin requests
     * 
     * @param registry CORS registry for configuration
     */
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:5173", // Vite development server
                        "https://reflectly-ajb7dchaaxewgte0.southeastasia-01.azurewebsites.net" // Production URL
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "Cache-Control")
                .allowCredentials(true)
                .maxAge(3600); // Cache preflight response for 1 hour
    }
}