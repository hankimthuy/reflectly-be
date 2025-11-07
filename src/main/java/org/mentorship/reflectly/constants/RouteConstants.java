package org.mentorship.reflectly.constants;

public final class RouteConstants {

    // Private constructor to prevent instantiation
    private RouteConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ==================== PUBLIC ROUTES (permitAll) ====================

    public static final String[] PUBLIC_ROUTES = {
            // Root path for SPA
            "/", "/api/users/profile",
            // Documentation
            "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
            // Monitoring
            "/actuator/health", "/actuator/info",
            // Static resources - handled by WebMvcConfig
            "/assets/**", "/static/**", "/*.html", "/*.js", "/*.css", "/*.svg", "/*.ico",
            // Public API
            "/api/public/**"
    };

}