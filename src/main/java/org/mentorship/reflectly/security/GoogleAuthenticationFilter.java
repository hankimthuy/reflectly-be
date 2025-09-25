package org.mentorship.reflectly.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.service.UserService;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Google Authentication Filter that processes Google ID tokens.
 * This filter runs once per request and handles Google OAuth authentication.
 * Flow:
 * 1. Extract Google ID token from Authorization header
 * 2. Validate Google ID token
 * 3. Find or create user in database
 * 4. Set authentication context
 */
@Component
@RequiredArgsConstructor
public class GoogleAuthenticationFilter extends OncePerRequestFilter {

    private final UserService userService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract Google ID token from Authorization header
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String googleIdToken = authHeader.substring(7);

                // Process Google authentication
                processGoogleAuthentication(googleIdToken);
                logger.info("Google authentication successful for request: " + request.getRequestURI());
            } else {
                logger.debug("No valid Authorization header found for request: " + request.getRequestURI());
            }

        } catch (Exception e) {
            // Log error and clear any existing authentication
            logger.warn("Google authentication failed for request " + request.getRequestURI() + ": " + e.getMessage());
            SecurityContextHolder.clearContext();
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Process Google ID token authentication using AuthService
     */
    private void processGoogleAuthentication(String googleIdToken) throws GeneralSecurityException, IOException {
        // Use AuthService to validate token and get payload
        GoogleIdToken.Payload payload = userService.validateGoogleToken(googleIdToken);

        // Use AuthService to find or create user
        UserEntity user = userService.findOrCreateUser(payload);

        // Set authentication context
        SecurityContextHolder.getContext().setAuthentication(new GoogleAuthenticationToken(user, googleIdToken));
    }
}