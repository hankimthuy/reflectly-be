package org.mentorship.reflectly.constants;

/**
 * Constants cho routing và security configuration
 * Simplified grouping cho tất cả route patterns
 */
public final class RouteConstants {
    
    // Private constructor to prevent instantiation
    private RouteConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    // ==================== SPA ROUTES ====================
    
    public static final String ROOT = "/";
    
    public static final String SPA_CATCH_ALL = "/{path:^(?!api|assets|static|swagger-ui|v3|actuator).*$}/**";
    
    // ==================== PUBLIC ROUTES (permitAll) ====================
    
    public static final String[] PUBLIC_ROUTES = {
        // Documentation
        "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
        // Monitoring
        "/actuator/health", "/actuator/info",
        // Static resources
        "/", "/assets/**", "/static/**", "/*.html", "/*.js", "/*.css", "/*.svg",
        // Public API
        "/api/public/**"
    };
    
    // ==================== API ROUTES (authenticated) ====================
    
    public static final String[] API_ROUTES = {
        "/api/users/**",
        "/api/journal/**",
        "/api/admin/**"
    };
}
