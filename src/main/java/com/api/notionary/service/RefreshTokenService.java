package com.api.notionary.service;

import com.api.notionary.entity.RefreshToken;
import com.api.notionary.entity.User;
import com.api.notionary.exception.TokenRefreshException;
import com.api.notionary.exception.UserNotFoundException;
import com.api.notionary.repository.RefreshTokenRepository;
import com.api.notionary.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${refresh.token.expiration.sec}")
    private Long refreshTokenDurationSec;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Autowired
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository)  {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepository.findById(userId).get());
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenDurationSec));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }

        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException(String.format("User with id [%s] not found", userId)));
        return refreshTokenRepository.deleteByUser(user);
    }
}
