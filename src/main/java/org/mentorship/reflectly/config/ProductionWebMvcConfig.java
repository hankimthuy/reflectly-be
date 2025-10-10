package org.mentorship.reflectly.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.PathResourceResolver;
import java.io.IOException;

/**
 * Production-specific WebMvcConfigurer for SPA routing
 * This configuration only applies when running with 'prod' profile
 * Handles SPA routing fallback to index.html for client-side routing
 */
@Configuration
@Profile("prod")
public class ProductionWebMvcConfig implements WebMvcConfigurer {

    /**
     * @param registry Resource handler registry for configuration
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configure SPA routing fallback
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        
                        // For SPA routing, fallback to index.html for non-API, non-static requests
                        if (!resourcePath.startsWith("api/") && 
                            !resourcePath.contains(".") && 
                            !resourcePath.startsWith("static/")) {
                            return location.createRelative("index.html");
                        }
                        
                        return null;
                    }
                });
    }
}