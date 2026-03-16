package com.api.notionary.service;

import com.api.notionary.dto.ApiResponseWrapper;
import com.api.notionary.dto.payload.request.user.SignInRequest;
import com.api.notionary.dto.payload.request.user.SignUpRequest;
import com.api.notionary.dto.token.AuthResultDto;
import com.api.notionary.entity.ConfirmationToken;
import com.api.notionary.entity.RefreshToken;
import com.api.notionary.entity.User;
import com.api.notionary.entity.UserRole;
import com.api.notionary.exception.EntityNotFoundException;
import com.api.notionary.exception.TokenExpiredException;
import com.api.notionary.exception.TokenRefreshException;
import com.api.notionary.exception.UserAlreadyActivatedException;
import com.api.notionary.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private UserService userService;
    @Mock
    private ConfirmationTokenService confirmationTokenService;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        user = new User("John", "Doe", "john@example.com", "encoded",
                Instant.now(), UserRole.ROLE_USER);
        user.setId(1L);
        user.setEnabled(true);
        refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-123");
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));
    }

    @Test
    void signUp_shouldPublishEventAndReturnMessage() {
        SignUpRequest request = new SignUpRequest("John", "Doe", "john@example.com", "password123");
        when(userService.signUpUser(any(User.class))).thenReturn("confirmation-token");

        ApiResponseWrapper result = authenticationService.signUp(request);

        assertThat(result.message()).contains("registered successfully");
        assertThat(result.message()).contains("email");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).signUpUser(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("john@example.com");
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void signIn_shouldAuthenticateAndReturnTokens() {
        SignInRequest request = new SignInRequest("john@example.com", "password123");
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authToken);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(1L)).thenReturn(refreshToken);

        AuthResultDto result = authenticationService.signIn(request);

        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-123");
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("john@example.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void signIn_shouldNormalizeEmail() {
        SignInRequest request = new SignInRequest("  JOHN@Example.COM  ", "pass");
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authToken);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt");
        when(refreshTokenService.createRefreshToken(1L)).thenReturn(refreshToken);

        authenticationService.signIn(request);

        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void signIn_shouldThrow_whenUserNotFoundAfterAuth() {
        SignInRequest request = new SignInRequest("john@example.com", "pass");
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authToken);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.signIn(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("john@example.com");
    }

    @Test
    void refreshToken_shouldReturnNewTokens() {
        when(refreshTokenService.findByToken("old-refresh")).thenReturn(Optional.of(refreshToken));
        RefreshToken newRefresh = new RefreshToken();
        newRefresh.setToken("new-refresh");
        newRefresh.setUser(user);
        when(refreshTokenService.createRefreshToken(1L)).thenReturn(newRefresh);
        when(jwtService.generateToken(user)).thenReturn("new-jwt");

        AuthResultDto result = authenticationService.refreshToken("old-refresh");

        assertThat(result.accessToken()).isEqualTo("new-jwt");
        assertThat(result.refreshToken()).isEqualTo("new-refresh");
        assertThat(result.email()).isEqualTo("john@example.com");
        verify(refreshTokenService).deleteByToken("old-refresh");
        verify(refreshTokenService).verifyExpiration(refreshToken);
    }

    @Test
    void refreshToken_shouldThrow_whenTokenNotInDatabase() {
        when(refreshTokenService.findByToken("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.refreshToken("invalid"))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("not in database");
    }

    @Test
    void confirmToken_shouldActivateUserAndReturnMessage() {
        user.setEnabled(false);
        ConfirmationToken confToken = new ConfirmationToken("token", Instant.now(),
                Instant.now().plus(1, ChronoUnit.DAYS), user);
        when(confirmationTokenService.getToken("token")).thenReturn(Optional.of(confToken));

        ApiResponseWrapper result = authenticationService.confirmToken("token");

        assertThat(user.isEnabled()).isTrue();
        assertThat(result.message()).contains("activated");
        verify(confirmationTokenService).deleteTokenFromDatabase(confToken);
    }

    @Test
    void confirmToken_shouldThrow_whenUserAlreadyEnabled() {
        ConfirmationToken confToken = new ConfirmationToken("token", Instant.now(),
                Instant.now().plus(1, ChronoUnit.DAYS), user);
        when(confirmationTokenService.getToken("token")).thenReturn(Optional.of(confToken));

        assertThatThrownBy(() -> authenticationService.confirmToken("token"))
                .isInstanceOf(UserAlreadyActivatedException.class)
                .hasMessageContaining("already confirmed");
    }

    @Test
    void confirmToken_shouldThrow_whenTokenExpired() {
        user.setEnabled(false);
        ConfirmationToken confToken = new ConfirmationToken("token", Instant.now().minus(2, ChronoUnit.DAYS),
                Instant.now().minus(1, ChronoUnit.DAYS), user);
        when(confirmationTokenService.getToken("token")).thenReturn(Optional.of(confToken));

        assertThatThrownBy(() -> authenticationService.confirmToken("token"))
                .isInstanceOf(TokenExpiredException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void resendConfirmationEmail_shouldPublishEventAndReturnMessage() {
        user.setEnabled(false);
        when(userService.getUserByEmail("john@example.com")).thenReturn(user);
        when(userService.generateNewConfirmationToken(user)).thenReturn("new-token");

        ApiResponseWrapper result = authenticationService.resendConfirmationEmail("john@example.com");

        assertThat(result.message()).contains("new confirmation email");
        verify(userService).getUserByEmail("john@example.com");
        verify(userService).generateNewConfirmationToken(user);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void resendConfirmationEmail_shouldThrow_whenUserAlreadyEnabled() {
        when(userService.getUserByEmail("john@example.com")).thenReturn(user);

        assertThatThrownBy(() -> authenticationService.resendConfirmationEmail("john@example.com"))
                .isInstanceOf(UserAlreadyActivatedException.class)
                .hasMessageContaining("already confirmed");
    }

    @Test
    void logout_shouldDeleteToken_whenNonNullNonBlank() {
        authenticationService.logout("refresh-token");

        verify(refreshTokenService).deleteByToken("refresh-token");
    }

    @Test
    void logout_shouldDoNothing_whenNull() {
        authenticationService.logout(null);
        verify(refreshTokenService, never()).deleteByToken(any());
    }

    @Test
    void logout_shouldDoNothing_whenBlank() {
        authenticationService.logout("   ");
        verify(refreshTokenService, never()).deleteByToken(any());
    }
}
