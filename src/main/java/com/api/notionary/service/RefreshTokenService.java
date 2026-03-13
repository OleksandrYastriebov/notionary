package com.api.notionary.service;

import com.api.notionary.entity.RefreshToken;
import com.api.notionary.exception.TokenRefreshException;
import com.api.notionary.exception.UserNotFoundException;
import com.api.notionary.repository.RefreshTokenRepository;
import com.api.notionary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class RefreshTokenService {
    @Value("${token.refresh.expiration.sec}")
    private Long refreshTokenDurationSec;

    @Value("${token.refresh.max.sessions.count}")
    private int maxSessions;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findAllByUserIdOrderByExpiresAtAsc(userId);

        if (activeTokens.size() >= maxSessions) {
            refreshTokenRepository.delete(activeTokens.getFirst());
        }

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format("No User found with the following ID: %s", userId))));
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenDurationSec));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void verifyExpiration(RefreshToken token) {
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token was expired. Please make a new signin request");
        }
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
