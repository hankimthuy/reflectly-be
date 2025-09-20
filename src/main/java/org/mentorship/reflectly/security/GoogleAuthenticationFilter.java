package org.mentorship.reflectly.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.repository.UserRepository;
import org.mentorship.reflectly.service.JwtService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * Google Authentication Filter that processes Google ID tokens.
 * This filter runs once per request and handles Google OAuth authentication.
 * 
 * Flow:
 * 1. Extract Google ID token from Authorization header
 * 2. Validate Google ID token
 * 3. Find or create user in database
 * 4. Generate internal JWT token
 * 5. Set authentication context
 */
@Component
public class GoogleAuthenticationFilter extends OncePerRequestFilter {

    private final GoogleIdTokenVerifier googleVerifier;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public GoogleAuthenticationFilter(GoogleIdTokenVerifier googleVerifier, 
                                   UserRepository userRepository, 
                                   JwtService jwtService) {
        this.googleVerifier = googleVerifier;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Extract Google ID token from Authorization header
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String googleIdToken = authHeader.substring(7); // Remove "Bearer " prefix
                
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
     * Process Google ID token authentication
     */
    private void processGoogleAuthentication(String googleIdToken) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = googleVerifier.verify(googleIdToken);
        if (idToken == null) {
            throw new IllegalArgumentException("Invalid Google ID Token");
        }
        
        GoogleIdToken.Payload payload = idToken.getPayload();
        UserEntity user = findOrCreateUser(payload);
        
        String internalJwtToken = jwtService.generateToken(payload.getEmail(), Map.of(
            "userId", user.getId().toString(),
            "fullName", user.getFullName(),
            "pictureUrl", user.getPictureUrl()
        ));
        
        SecurityContextHolder.getContext().setAuthentication(
            new GoogleAuthenticationToken(user, googleIdToken, internalJwtToken)
        );
    }

    /**
     * Find existing user by email or create a new one
     */
    private UserEntity findOrCreateUser(GoogleIdToken.Payload payload) {
        String email = payload.getEmail();
        String fullName = getStringOrEmpty(payload, "name");
        String pictureUrl = getStringOrEmpty(payload, "picture");
        
        return userRepository.findByEmail(email)
            .map(user -> {
                // Update existing user with safe values
                String safeFullName = fullName.isEmpty() ? user.getFullName() : fullName;
                String safePictureUrl = pictureUrl.isEmpty() ? user.getPictureUrl() : pictureUrl;
                user.updateProfile(safeFullName, safePictureUrl);
                return userRepository.save(user);
            })
            .orElseGet(() -> {
                // Create new user
                UserEntity newUser = new UserEntity(email, fullName, pictureUrl);
                return userRepository.save(newUser);
            });
    }
    
    /**
     * Extract string value from payload or return empty string if null
     */
    private String getStringOrEmpty(GoogleIdToken.Payload payload, String key) {
        String value = (String) payload.get(key);
        return value != null ? value : "";
    }
}