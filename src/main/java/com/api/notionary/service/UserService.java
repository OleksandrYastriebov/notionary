package com.api.notionary.service;

import com.api.notionary.entity.ConfirmationToken;
import com.api.notionary.entity.User;
import com.api.notionary.exception.UserAlreadyExistsException;
import com.api.notionary.exception.UserNotFoundException;
import com.api.notionary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    @Value("${token.confirmation.expiration.days}")
    private int tokenExpirationDays;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public String signUpUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("Email already taken.");
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);
        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(tokenExpirationDays),
                user
        );

        confirmationTokenService.saveConfirmationToken(confirmationToken);
        return token;
    }

    @Transactional
    @PreAuthorize("#id == #currentUser.id or hasRole('ROLE_ADMIN')")
    public void deleteUserById(Long id, User currentUser) {
        User userToDelete = getUserEntityById(id);

        String randomHash = UUID.randomUUID().toString().substring(0, 8);
        userToDelete.setEmail("deleted_" + randomHash + "@notionary.deleted");
        userToDelete.setFirstName("Deleted");
        userToDelete.setLastName("User");

        userToDelete.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        userToDelete.setEnabled(false);
        userToDelete.setDeleted(true);

        refreshTokenService.deleteByUserId(userToDelete.getId());
        log.info("User with email {} was anonymized and soft-deleted.", currentUser.getEmail());
    }

    @Transactional
    public void cleanupUnverifiedUsers() {
        List<Long> idsToDelete = userRepository.findIdsOfExpiredAndDisabledUsers();
        if (idsToDelete.isEmpty()) {
            log.info("No expired unverified users found for cleanup.");
            return;
        }
        confirmationTokenService.deleteTokensByUserIds(idsToDelete);
        userRepository.bulkDeleteByIds(idsToDelete);
        log.info("Cleanup Job: Successfully removed {} unverified expired users.", idsToDelete.size());
    }

    private User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id %s can not be found.", id)));
    }
}
