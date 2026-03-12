package com.api.notionary.service;

import com.api.notionary.entity.ConfirmationToken;
import com.api.notionary.repository.ConfirmationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    @Transactional
    public void saveConfirmationToken(ConfirmationToken confirmationToken) {
        confirmationTokenRepository.save(confirmationToken);
    }

    public Optional<ConfirmationToken> getToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    @Transactional
    public void deleteTokensByUserIds(List<Long> userIds) {
        confirmationTokenRepository.deleteByUserIds(userIds);
    }

    @Transactional
    public void deleteTokenFromDatabase(ConfirmationToken token) {
        confirmationTokenRepository.delete(token);
    }
}
