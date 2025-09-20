package org.mentorship.reflectly.security;

import org.mentorship.reflectly.model.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom Authentication implementation for Google OAuth authentication.
 * Contains user information and Google ID token for authentication context.
 */
public class GoogleAuthenticationToken implements Authentication {

    private final UserEntity user;
    private final String googleIdToken;
    private final String internalJwtToken;
    private boolean authenticated = true;

    /**
     * Constructor for authenticated user
     */
    public GoogleAuthenticationToken(UserEntity user, String googleIdToken, String internalJwtToken) {
        this.user = user;
        this.googleIdToken = googleIdToken;
        this.internalJwtToken = internalJwtToken;
        this.authenticated = true;
    }

    /**
     * Get user entity from authentication context
     */
    public UserEntity getUser() {
        return user;
    }

    /**
     * Get Google ID token
     */
    public String getGoogleIdToken() {
        return googleIdToken;
    }

    /**
     * Get internal JWT token
     */
    public String getInternalJwtToken() {
        return internalJwtToken;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // For now, return empty authorities
        // Can be extended to include roles/permissions
        return Collections.emptyList();
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
}
