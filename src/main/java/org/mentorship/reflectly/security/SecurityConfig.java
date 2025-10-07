package org.mentorship.reflectly.security;

import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.constants.RouteConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final GoogleAuthenticationConverter googleAuthenticationConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(RouteConstants.PUBLIC_ROUTES)
                        .permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(configurer -> configurer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(googleAuthenticationConverter)));

        return http.build();
    }

}