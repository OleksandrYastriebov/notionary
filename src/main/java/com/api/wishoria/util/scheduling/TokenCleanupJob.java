package com.api.wishoria.util.scheduling;

import com.api.wishoria.repository.PasswordResetTokenRepository;
import com.api.wishoria.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
@Component
public class TokenCleanupJob {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    /**
     * Remove expired Refresh Tokens from the database.
     * Run cleanup job every night
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        refreshTokenRepository.deleteAllExpiredTokens();
        log.info("Successfully cleaned up expired refresh tokens.");
    }

    /**
     * Remove expired Password Reset Tokens from the database.
     * Runs every night at 03:00 AM
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredPasswordResetTokens() {
        passwordResetTokenRepository.deleteAllByExpiresAtBefore(Instant.now());
        log.info("Successfully cleaned up expired password reset tokens.");
    }
}
