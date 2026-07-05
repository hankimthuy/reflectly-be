package org.mentorship.reflectly.security;

import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.constants.RouteConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    private final GoogleAuthenticationConverter googleAuthenticationConverter;
    private final JwtExpirationFilter jwtExpirationFilter;
    private final BackendJwtAuthenticationFilter backendJwtAuthenticationFilter;
    private final PrivateNetworkAccessFilter privateNetworkAccessFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(privateNetworkAccessFilter, SecurityContextHolderFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS at
                // security layer
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Allow public routes (Swagger, static resources, etc.)
                        .requestMatchers(RouteConstants.PUBLIC_ROUTES).permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated())
                .addFilterBefore(backendJwtAuthenticationFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(jwtExpirationFilter, BearerTokenAuthenticationFilter.class)
                .oauth2ResourceServer(configurer -> configurer
                        .bearerTokenResolver(skipIfAlreadyAuthenticated())
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(googleAuthenticationConverter)));

        return http.build();
    }

    /**
     * Custom BearerTokenResolver that returns null when the request is already
     * authenticated by BackendJwtAuthenticationFilter, preventing Spring's
     * oauth2 BearerTokenAuthenticationFilter from re-processing backend JWTs.
     */
    private BearerTokenResolver skipIfAlreadyAuthenticated() {
        DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();
        return request -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                return null;
            }
            return defaultResolver.resolve(request);
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        if (origins.contains("*")) {
            configuration.setAllowedOriginPatterns(List.of("*"));
        } else {
            configuration.setAllowedOrigins(origins);
        }

        // Allow all HTTP methods (OPTIONS required for CORS preflight requests)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Expose headers (Safari does not support wildcard '*' with credentials)
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "Content-Length"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
