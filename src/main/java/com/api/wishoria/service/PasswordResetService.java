package com.api.wishoria.service;

import com.api.wishoria.entity.PasswordResetToken;
import com.api.wishoria.entity.User;
import com.api.wishoria.event.PasswordRecoveryEvent;
import com.api.wishoria.exception.TokenInvalidException;
import com.api.wishoria.repository.PasswordResetTokenRepository;
import com.api.wishoria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;


    @Transactional
    public void initiatePasswordReset(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }

        User userEntity = user.get();
        tokenRepository.deleteByUser(userEntity);
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, userEntity, Instant.now().plus(1, ChronoUnit.HOURS));
        tokenRepository.save(resetToken);

        eventPublisher.publishEvent(new PasswordRecoveryEvent(this, userEntity, token));
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenInvalidException("This password reset link is invalid or has already been used."));

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            tokenRepository.delete(resetToken);
            throw new TokenInvalidException("Token has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(resetToken);
    }
}