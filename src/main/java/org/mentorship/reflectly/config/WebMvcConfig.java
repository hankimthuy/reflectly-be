package org.mentorship.reflectly.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * WebMvcConfigurer configuration class for handling CORS, interceptors, and view controllers
 * This class centralizes web configuration for the Spring Boot application
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private Environment environment;

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
    /**
     * Configure view controllers for SPA (Single Page Application) routing
     * This method maps non-API routes to index.html to support client-side routing
     * Only applies in production environment to avoid interfering with development API calls
     * 
     * @param registry View controller registry for configuration
     */
    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
        // Only apply SPA routing in production environment
        if (isProductionEnvironment()) {
            // Map all non-API, non-static file routes to index.html for SPA routing
            registry.addViewController("/{path:[^\\.]*}")
                    .setViewName("forward:/index.html");
            
            // Map nested paths (e.g., /dashboard/settings) to index.html
            registry.addViewController("/**/{path:[^\\.]*}")
                    .setViewName("forward:/index.html");
        }
    }

    /**
     * Check if the application is running in production environment
     * 
     * @return true if running in production, false otherwise
     */
    private boolean isProductionEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("prod".equals(profile) || "production".equals(profile)) {
                return true;
            }
        }
        return false;
    }
}
