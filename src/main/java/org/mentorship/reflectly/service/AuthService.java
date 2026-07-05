package org.mentorship.reflectly.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.dto.AuthLoginResponseDto;
import org.mentorship.reflectly.dto.UserProfileRecord;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.google.client-id}")
    private String clientId;

    @Value("${app.google.client-secret}")
    private String clientSecret;

    /**
     * Exchange a Google Auth Code for an ID Token, then verify and issue a backend JWT.
     *
     * @param authCode the raw Google authorization code from the frontend
     * @return response containing backend JWT and user profile
     * @throws IllegalArgumentException if the Google Auth code or ID token is invalid
     */
    public AuthLoginResponseDto loginWithGoogle(String authCode) {
        GoogleIdToken idToken;
        try {
            // Exchange authorization code for tokens
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(), 
                    GsonFactory.getDefaultInstance(),
                    "https://oauth2.googleapis.com/token", 
                    clientId, 
                    clientSecret,
                    authCode, 
                    "postmessage") // Redirect URI used by react-oauth/google
                    .execute();

            String idTokenString = tokenResponse.getIdToken();
            idToken = googleIdTokenVerifier.verify(idTokenString);
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalArgumentException("Failed to verify Google Auth Code or ID token", e);
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
