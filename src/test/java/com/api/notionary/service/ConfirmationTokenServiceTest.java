package com.api.notionary.service;

import com.api.notionary.entity.ConfirmationToken;
import com.api.notionary.entity.User;
import com.api.notionary.entity.UserRole;
import com.api.notionary.repository.ConfirmationTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmationTokenServiceTest {

    @Mock
    private ConfirmationTokenRepository confirmationTokenRepository;

    @InjectMocks
    private ConfirmationTokenService confirmationTokenService;

    @Test
    void saveConfirmationToken_shouldDelegateToRepository() {
        User user = new User("John", "Doe", "john@example.com", "pass",
                LocalDateTime.now(), UserRole.ROLE_USER);
        ConfirmationToken token = new ConfirmationToken("token-value",
                LocalDateTime.now(), LocalDateTime.now().plusDays(7), user);

        confirmationTokenService.saveConfirmationToken(token);

        verify(confirmationTokenRepository).save(token);
    }

    @Test
    void getToken_shouldReturnOptionalFromRepository() {
        when(confirmationTokenRepository.findByToken("token-123")).thenReturn(Optional.empty());

        var result = confirmationTokenService.getToken("token-123");

        assertThat(result).isEmpty();
        verify(confirmationTokenRepository).findByToken("token-123");
    }

    @Test
    void getToken_shouldReturnPresentOptional_whenTokenExists() {
        User user = new User("John", "Doe", "john@example.com", "pass",
                LocalDateTime.now(), UserRole.ROLE_USER);
        ConfirmationToken token = new ConfirmationToken("token-123",
                LocalDateTime.now(), LocalDateTime.now().plusDays(7), user);
        when(confirmationTokenRepository.findByToken("token-123")).thenReturn(Optional.of(token));

        var result = confirmationTokenService.getToken("token-123");

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("token-123");
    }

    @Test
    void deleteTokensByUserIds_shouldCallRepository() {
        List<Long> userIds = List.of(1L, 2L);

        confirmationTokenService.deleteTokensByUserIds(userIds);

        verify(confirmationTokenRepository).deleteByUserIds(userIds);
    }

    @Test
    void deleteTokenFromDatabase_shouldCallRepository() {
        User user = new User("John", "Doe", "john@example.com", "pass",
                LocalDateTime.now(), UserRole.ROLE_USER);
        ConfirmationToken token = new ConfirmationToken("t", LocalDateTime.now(),
                LocalDateTime.now().plusDays(7), user);

        confirmationTokenService.deleteTokenFromDatabase(token);

        verify(confirmationTokenRepository).delete(token);
    }
}
