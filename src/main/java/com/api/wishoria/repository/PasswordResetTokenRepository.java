package com.api.wishoria.repository;

import com.api.wishoria.entity.PasswordResetToken;
import com.api.wishoria.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);

    void deleteAllByExpiryDateBefore(Instant now);
}