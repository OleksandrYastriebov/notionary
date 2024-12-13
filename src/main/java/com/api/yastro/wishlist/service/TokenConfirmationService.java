package com.api.yastro.wishlist.service;

import com.api.yastro.wishlist.entity.ConfirmationToken;
import com.api.yastro.wishlist.repository.ConfirmationTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TokenConfirmationService {
    private final ConfirmationTokenRepository confirmationTokenRepository;

    public TokenConfirmationService(ConfirmationTokenRepository confirmationTokenRepository) {
        this.confirmationTokenRepository = confirmationTokenRepository;
    }

    public void saveConfirmationToken(ConfirmationToken confirmationToken) {
        confirmationTokenRepository.save(confirmationToken);
    }

    public Optional<ConfirmationToken> getToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    public int setConfirmedAt(String token) {
        return confirmationTokenRepository.updateConfirmedAt(token, LocalDateTime.now());
    }

    public void deleteTokenFromDatabase(String token) {
        confirmationTokenRepository.deleteByToken(token);
    }
}
