package org.mentorship.reflectly.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration for Reflectly API documentation.
 * Provides API documentation and testing interface.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reflectly API")
                        .description("API for Reflectly - Personal Journal and Reflection App")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Reflectly Team")
                                .email("support@reflectly.com")
                                .url("https://reflectly.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("""
                    Enter your Google ID Token here.
                    
                    How to get Google ID Token:
                    1. Go to https://developers.google.com/oauthplayground/
                    2. Select 'Google OAuth2 API v2' 
                    3. Select scopes: 'openid email profile'
                    4. Click 'Authorize APIs'
                    5. Login with your Google account
                    6. Copy the 'id_token' value
                    7. Paste it here (without 'Bearer ' prefix)
                    
                    Or use Google Identity Services in your frontend:
                    - Load: https://accounts.google.com/gsi/client
                    - Initialize with your client_id
                    - Get token from callback response
                    """);
    }
}
