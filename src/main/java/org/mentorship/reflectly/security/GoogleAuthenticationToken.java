package org.mentorship.reflectly.security;

import lombok.Getter;
import org.mentorship.reflectly.model.UserEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom Authentication implementation for Google OAuth authentication.
 * Contains user information and Google ID token for authentication context.
 */
public class GoogleAuthenticationToken extends UsernamePasswordAuthenticationToken {

    @Getter
    private final UserEntity user;
    private final String googleIdToken;
    @Getter
    private final Jwt jwt;
    private boolean authenticated;

    public GoogleAuthenticationToken(UserEntity user, String googleIdToken, Jwt jwt) {
        super(user, googleIdToken, Collections.emptyList());
        this.user = user;
        this.googleIdToken = googleIdToken;
        this.jwt = jwt;
        this.authenticated = true;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return super.getAuthorities();
    }

    @Override
    public Object getCredentials() {
        return googleIdToken;
    }

    @Override
    public Object getDetails() {
        return user;
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return user != null ? user.getEmail() : null;
    }

    public Long getTokenExpiresAt() {
        if (jwt != null && jwt.getExpiresAt() != null) {
            return jwt.getExpiresAt().getEpochSecond();
        }
        return null;
    }
}