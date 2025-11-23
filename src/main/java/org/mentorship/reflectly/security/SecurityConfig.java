package org.mentorship.reflectly.security;

import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.constants.RouteConstants;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final GoogleAuthenticationConverter googleAuthenticationConverter;
        private final CookieBearerTokenResolver cookieBearerTokenResolver;
        private final JwtExpirationFilter jwtExpirationFilter;
        private final JwtDecoder jwtDecoder;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS at
                                                                                                   // security layer
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(authorize -> authorize
                                                // Allow public routes (Swagger, static resources, etc.)
                                                .requestMatchers(RouteConstants.PUBLIC_ROUTES).permitAll()
                                                // All other requests require authentication
                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtExpirationFilter, BearerTokenAuthenticationFilter.class)
                                .oauth2ResourceServer(configurer -> configurer
                                                .bearerTokenResolver(cookieBearerTokenResolver)
                                                .jwt(jwt -> jwt.decoder(jwtDecoder).jwtAuthenticationConverter(
                                                                googleAuthenticationConverter)));

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // Allow specific origins
                configuration.setAllowedOriginPatterns(Arrays.asList(
                                "http://localhost:*",
                                "https://reflectly-ajb7dchaaxewgte0.southeastasia-01.azurewebsites.net"));

                // Allow all HTTP methods (OPTIONS required for CORS preflight requests)
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

                // Allow all headers
                configuration.setAllowedHeaders(Arrays.asList(
                                "Authorization",
                                "Content-Type",
                                "Cache-Control",
                                "Accept",
                                "X-Requested-With",
                                "Origin",
                                "Access-Control-Request-Method",
                                "Access-Control-Request-Headers"));

                // Expose headers
                configuration.setExposedHeaders(Arrays.asList(
                                "Access-Control-Allow-Origin",
                                "Access-Control-Allow-Credentials",
                                "Access-Control-Allow-Methods",
                                "Access-Control-Allow-Headers"));

                // Allow credentials (cookies, authorization headers)
                configuration.setAllowCredentials(true);

                // Cache preflight response for 1 hour
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

}