package com.api.notionary.service;

import com.api.notionary.entity.ConfirmationToken;
import com.api.notionary.repository.ConfirmationTokenRepository;
import org.springframework.stereotype.Service;

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

    public void deleteTokenFromDatabase(String token) {
        confirmationTokenRepository.deleteByToken(token);
    }
}
