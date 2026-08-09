package org.mentorship.reflectly.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mentorship.reflectly.dto.AuthLoginResponseDto;
import org.mentorship.reflectly.dto.UserProfileRecord;
import org.mentorship.reflectly.exception.LocalAuthDisabledException;
import org.mentorship.reflectly.model.UserEntity;
import org.mentorship.reflectly.security.JwtService;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Covers the local-credentials kill switch added to bound abuse before this app has a real
 * access/billing model — see app.auth.local-credentials-enabled (off by default in prod).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private GoogleIdTokenVerifier googleIdTokenVerifier;
    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(googleIdTokenVerifier, userService, jwtService);
    }

    @Test
    void loginWithCredentials_throwsWhenLocalAuthDisabled() {
        ReflectionTestUtils.setField(authService, "localCredentialsEnabled", false);

        assertThrows(LocalAuthDisabledException.class,
                () -> authService.loginWithCredentials("someone", "password"));
        verifyNoInteractions(userService);
    }

    @Test
    void signup_throwsWhenLocalAuthDisabled() {
        ReflectionTestUtils.setField(authService, "localCredentialsEnabled", false);

        assertThrows(LocalAuthDisabledException.class,
                () -> authService.signup("someone", "password", "Full Name"));
        verifyNoInteractions(userService);
    }

    @Test
    void signup_succeedsWhenLocalAuthEnabled() {
        ReflectionTestUtils.setField(authService, "localCredentialsEnabled", true);

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail(null);
        user.setUsername("someone");

        when(userService.createUser("someone", "password", "Full Name")).thenReturn(user);
        when(jwtService.generateToken("1", null)).thenReturn("jwt-token");
        when(userService.toProfileRecord(user)).thenReturn(
                new UserProfileRecord("1", null, "someone", "Full Name", "", false, List.of(), false));

        AuthLoginResponseDto response = authService.signup("someone", "password", "Full Name");

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().username()).isEqualTo("someone");
    }
}
