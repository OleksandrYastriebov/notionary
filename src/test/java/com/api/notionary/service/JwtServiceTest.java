package com.api.notionary.service;

import com.api.notionary.entity.User;
import com.api.notionary.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;
    private static final String BASE64_KEY = Base64.getEncoder().encodeToString(
            "0123456789012345678901234567890123456789012345678901234567890123".getBytes());
    private static final long EXPIRATION_MS = 3600_000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSigningKey", BASE64_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", EXPIRATION_MS);
    }

    @Test
    void extractEmail_shouldReturnSubjectFromToken() {
        User user = new User("John", "Doe", "john@example.com", "pass",
                LocalDateTime.now(), UserRole.ROLE_USER);
        user.setId(1L);
        String token = jwtService.generateToken(user);

        String email = jwtService.extractEmail(token);

        assertThat(email).isEqualTo("john@example.com");
    }

    @Test
    void generateToken_shouldProduceValidToken_forUser() {
        User user = new User("Jane", "Doe", "jane@example.com", "secret",
                LocalDateTime.now(), UserRole.ROLE_USER);
        user.setId(2L);

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractEmail(token)).isEqualTo("jane@example.com");
    }

    @Test
    void generateToken_shouldIncludeEmailInClaims_forUser() {
        User user = new User("John", "Doe", "john@example.com", "pass",
                LocalDateTime.now(), UserRole.ROLE_USER);
        user.setId(1L);

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractEmail(token)).isEqualTo("john@example.com");
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenEmailMatchesAndNotExpired() {
        User user = new User("John", "Doe", "john@example.com", "pass",
                LocalDateTime.now(), UserRole.ROLE_USER);
        user.setId(1L);
        String token = jwtService.generateToken(user);

        boolean valid = jwtService.isTokenValid(token, user);

        assertThat(valid).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenEmailDoesNotMatch() {
        User user = new User("John", "Doe", "john@example.com", "pass",
                LocalDateTime.now(), UserRole.ROLE_USER);
        user.setId(1L);
        User otherUser = new User("Jane", "Doe", "jane@example.com", "pass",
                LocalDateTime.now(), UserRole.ROLE_USER);
        otherUser.setId(2L);
        String token = jwtService.generateToken(user);

        boolean valid = jwtService.isTokenValid(token, otherUser);

        assertThat(valid).isFalse();
    }

    @Test
    void generateToken_shouldWorkWithPlainUserDetails() {
        User user = new User("John", "Doe", "user@test.com", "pass",
                LocalDateTime.now(), UserRole.ROLE_USER);
        user.setId(1L);

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractEmail(token)).isEqualTo("user@test.com");
    }
}
