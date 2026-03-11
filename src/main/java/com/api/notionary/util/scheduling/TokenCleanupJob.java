package com.api.notionary.util.scheduling;

import com.api.notionary.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class TokenCleanupJob {

    private final RefreshTokenRepository refreshTokenRepository;

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
}
