package com.api.notionary.service;

import com.api.notionary.entity.RefreshToken;
import com.api.notionary.entity.User;
import com.api.notionary.entity.UserRole;
import com.api.notionary.exception.TokenRefreshException;
import com.api.notionary.exception.UserNotFoundException;
import com.api.notionary.repository.RefreshTokenRepository;
import com.api.notionary.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final Long DURATION_SEC = 86400L;
    private static final int MAX_SESSIONS = 5;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationSec", DURATION_SEC);
        ReflectionTestUtils.setField(refreshTokenService, "maxSessions", MAX_SESSIONS);
        user = new User("John", "Doe", "john@example.com", "pass",
                Instant.now(), UserRole.ROLE_USER);
        user.setId(1L);
    }

    @Test
    void findByToken_shouldReturnOptionalFromRepository() {
        RefreshToken token = new RefreshToken();
        token.setToken("token-123");
        when(refreshTokenRepository.findByToken("token-123")).thenReturn(Optional.of(token));

        Optional<RefreshToken> result = refreshTokenService.findByToken("token-123");

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("token-123");
    }

    @Test
    void createRefreshToken_shouldSaveAndReturnToken_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findAllByUserIdOrderByExpiresAtAsc(1L)).thenReturn(List.of());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> {
            RefreshToken t = inv.getArgument(0);
            t.setId(100L);
            return t;
        });

        RefreshToken result = refreshTokenService.createRefreshToken(1L);

        assertThat(result).isNotNull();
        assertThat(result.getUser()).isSameAs(user);
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getExpiresAt()).isAfter(Instant.now());
        verify(refreshTokenRepository).save(result);
    }

    @Test
    void createRefreshToken_shouldEvictOldest_whenMaxSessionsReached() {
        RefreshToken oldToken = new RefreshToken();
        oldToken.setId(1L);
        List<RefreshToken> existing = List.of(oldToken, new RefreshToken(), new RefreshToken(), new RefreshToken(), new RefreshToken());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findAllByUserIdOrderByExpiresAtAsc(1L)).thenReturn(existing);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        refreshTokenService.createRefreshToken(1L);

        verify(refreshTokenRepository).delete(oldToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.createRefreshToken(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void verifyExpiration_shouldThrowAndDelete_whenTokenExpired() {
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(Instant.now().minus(1, ChronoUnit.SECONDS));

        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(token))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("expired");
        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void verifyExpiration_shouldDoNothing_whenTokenNotExpired() {
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));

        refreshTokenService.verifyExpiration(token);

        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void deleteByToken_shouldCallRepository() {
        refreshTokenService.deleteByToken("token-to-delete");

        verify(refreshTokenRepository).deleteByToken("token-to-delete");
    }

    @Test
    void deleteByUserId_shouldCallRepository() {
        refreshTokenService.deleteByUserId(1L);

        verify(refreshTokenRepository).deleteByUserId(1L);
    }
}
