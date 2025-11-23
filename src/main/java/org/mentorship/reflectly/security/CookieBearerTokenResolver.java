package org.mentorship.reflectly.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

/**
 * Custom BearerTokenResolver to extract JWT token from cookie or Authorization header.
 * This resolver is also injected into JwtExpirationFilter to avoid duplicate token extraction logic.
 */
@Component
public class CookieBearerTokenResolver implements BearerTokenResolver {

    private static final String COOKIE_NAME = "auth_token";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Resolves JWT token from request (required by BearerTokenResolver interface).
     * Cookie first, then Authorization header.
     */
    @Override
    public String resolve(HttpServletRequest request) {
        String token = extractTokenFromCookie(request);
        
        if (token == null) {
            token = extractTokenFromHeader(request);
        }
        
        return token;
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}

