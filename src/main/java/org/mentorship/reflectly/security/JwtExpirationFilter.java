package org.mentorship.reflectly.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.constants.RouteConstants;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

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

    /**
     * Check if the request path is a public route that doesn't require authentication.
     * Public routes are already configured in SecurityConfig with permitAll(), but this check prevents unnecessary JWT decoding for routes that don't need it.
     */
    private boolean isPublicPath(String path) {
        for (String publicRoute : RouteConstants.PUBLIC_ROUTES) {
            String routePattern = publicRoute.replace("/**", "").replace("**", "");
            if (path.startsWith(routePattern) || path.equals(routePattern)) {
                return true;
            }
        }
        return false;
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
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}

