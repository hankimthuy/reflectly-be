package org.mentorship.reflectly.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final GoogleAuthenticationConverter googleAuthenticationConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(this::configureAuthorization)
                .oauth2ResourceServer(configurer -> configurer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(googleAuthenticationConverter)));

        return http.build();
    }

    /**
     * Cấu hình authorization rules cho các endpoints
     */
    private void configureAuthorization(
            org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorize) {
        
        authorize
                .requestMatchers(getEndpoints(EndpointType.SWAGGER)).permitAll()
                .requestMatchers(getEndpoints(EndpointType.MONITORING)).permitAll()
                .requestMatchers(getEndpoints(EndpointType.STATIC_RESOURCES)).permitAll()
                .requestMatchers(getEndpoints(EndpointType.PUBLIC_API)).permitAll()
                .requestMatchers(getEndpoints(EndpointType.USER_API)).authenticated()
                .requestMatchers(getEndpoints(EndpointType.JOURNAL_API)).authenticated()
                .requestMatchers(getEndpoints(EndpointType.ADMIN_API)).hasRole("ADMIN")
                .anyRequest().authenticated();
    }

    private String[] getEndpoints(EndpointType type) {
        return switch (type) {
            case SWAGGER -> new String[]{
                    "/swagger-ui/**", 
                    "/v3/api-docs/**", 
                    "/swagger-ui.html"
            };
            case MONITORING -> new String[]{
                    "/actuator/health", 
                    "/actuator/info"
            };
            case STATIC_RESOURCES -> new String[]{
                    "/",
                    "/assets/**",
                    "/static/**",
                    "/*.html",
                    "/*.js",
                    "/*.css",
                    "/*.svg"
            };
            case PUBLIC_API -> new String[]{
                    "/api/public/**"
            };
            case USER_API -> new String[]{
                    "/api/users/**"
            };
            case JOURNAL_API -> new String[]{
                    "/api/journal/**"
            };
            case ADMIN_API -> new String[]{
                    "/api/admin/**"
            };
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173", // Vite dev server
            "https://reflectly-ajb7dchaaxewgte0.southeastasia-01.azurewebsites.net" // Production
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}