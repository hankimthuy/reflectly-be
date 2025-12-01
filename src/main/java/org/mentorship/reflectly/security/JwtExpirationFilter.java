package org.mentorship.reflectly.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.dto.ErrorResponseDto;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

/**
 * JWT Expiration Filter to validate token expiration after authentication.
 */
@Component
@RequiredArgsConstructor
public class JwtExpirationFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Retrieve the "Authorization" header from the HTTP request
        String authorization = request.getHeader("Authorization");

        // Check if the "Authorization" header is present and starts with "Bearer "
        if (authorization != null && authorization.startsWith("Bearer ")) {
            // Extract the JWT token by removing the "Bearer " prefix
            String token = authorization.substring(7);
            try {
                // Decode the JWT token using the JwtDecoder
                Jwt jwt = jwtDecoder.decode(token);

                // Check if the JWT token is expired
                if (isJwtExpired(jwt)) {
                    sendUnauthorizedResponse(response, "JWT token expired");
                    return;
                }
            } catch (JwtException e) {
                sendUnauthorizedResponse(response, "Invalid JWT token");
                return;
            }
        }

        // Continue the filter chain if the token is valid or no token is provided
        filterChain.doFilter(request, response);
    }

    private boolean isJwtExpired(Jwt jwt) {
        Instant expiresAt = jwt.getExpiresAt();
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        ErrorResponseDto error = ErrorResponseDto.builder().message(message).build();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}

