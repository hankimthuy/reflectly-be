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

/** * JWT Expiration Filter to validate token expiration and cookie age older than 1 day before authentication. */
@Component
@RequiredArgsConstructor
public class JwtExpirationFilter extends OncePerRequestFilter {

    private static final long COOKIE_MAX_AGE_SECONDS = 86400L; // 1 day

    private final CookieBearerTokenResolver cookieBearerTokenResolver;
    private final JwtDecoder jwtDecoder;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain)
            throws ServletException, IOException {

        String token = cookieBearerTokenResolver.resolve(request);

        if (token != null && !token.isEmpty()) {
            try {
                Jwt jwt = jwtDecoder.decode(token);

                if (isJwtExpired(jwt)) {
                    sendUnauthorizedResponse(response, "JWT token expired");
                    return;
                }

                if (isCookieExpired(jwt)) {
                    sendUnauthorizedResponse(response, "Cookie expired (older than 1 day)");
                    return;
                }

            } catch (JwtException e) {
                sendUnauthorizedResponse(response, "Invalid JWT token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isJwtExpired(Jwt jwt) {
        Instant expiresAt = jwt.getExpiresAt();
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    private boolean isCookieExpired(Jwt jwt) {
        Instant issuedAt = jwt.getIssuedAt();
        if (issuedAt == null) {
            return false;
        }
        Instant maxCookieAge = Instant.now().minusSeconds(COOKIE_MAX_AGE_SECONDS);
        return issuedAt.isBefore(maxCookieAge);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .message(message)
                .build();
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}

