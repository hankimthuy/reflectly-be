package org.mentorship.reflectly.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.dto.AuthLoginResponseDto;
import org.mentorship.reflectly.dto.UserProfileRecord;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.security.JwtService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Orchestrates the Google token exchange flow:
 * 1. Verify the Google ID token
 * 2. Find or create the user in the database
 * 3. Issue a backend JWT
 * 4. Return the JWT + user profile
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final UserService userService;
    private final JwtService jwtService;

    /**
     * Exchange a Google ID token for a backend JWT and user profile.
     *
     * @param googleIdTokenString the raw Google ID token from the frontend
     * @return response containing backend JWT and user profile
     * @throws IllegalArgumentException if the Google ID token is invalid
     */
    public AuthLoginResponseDto loginWithGoogle(String googleIdTokenString) {
        GoogleIdToken idToken;
        try {
            idToken = googleIdTokenVerifier.verify(googleIdTokenString);
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalArgumentException("Failed to verify Google ID token", e);
        }

        if (idToken == null) {
            throw new IllegalArgumentException("Invalid Google ID token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        UserEntity user = userService.findOrCreateUser(payload);

        return buildAuthResponse(user);
    }

    /**
     * Authenticate a user with username and password credentials.
     */
    public AuthLoginResponseDto loginWithCredentials(String username, String password) {
        UserEntity user = userService.authenticateByCredentials(username, password);
        return buildAuthResponse(user);
    }

    /**
     * Register a new user with username, password, and optional display name.
     */
    public AuthLoginResponseDto signup(String username, String password, String fullName) {
        UserEntity user = userService.createUser(username, password, fullName);
        return buildAuthResponse(user);
    }

    private AuthLoginResponseDto buildAuthResponse(UserEntity user) {
        String backendToken = jwtService.generateToken(
                user.getId().toString(),
                user.getEmail()
        );

        UserProfileRecord profile = userService.toProfileRecord(user);

        return AuthLoginResponseDto.builder()
                .token(backendToken)
                .user(profile)
                .build();
    }
}
