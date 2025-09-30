package org.mentorship.reflectly.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.service.UserService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;

@Component
@RequiredArgsConstructor
public class GoogleAuthenticationConverter implements Converter<Jwt, GoogleAuthenticationToken> {

    private final GoogleIdTokenVerifier googleVerifier;
    private final UserService userService;

    @Override
    public GoogleAuthenticationToken convert(Jwt jwt) {
        try {
            GoogleIdToken idToken = googleVerifier.verify(jwt.getTokenValue());
            UserEntity user = userService.findOrCreateUser(idToken.getPayload());

            // Set authentication context
            GoogleAuthenticationToken authentication = new GoogleAuthenticationToken(user, jwt.getTokenValue());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return authentication;
        } catch (GeneralSecurityException | java.io.IOException e) {
            throw new RuntimeException("Invalid Google ID token", e);
        }

    }
}