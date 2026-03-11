package com.api.notionary.service;

import com.api.notionary.entity.ConfirmationToken;
import com.api.notionary.entity.User;
import com.api.notionary.exception.UserAlreadyExistsException;
import com.api.notionary.exception.UserNotFoundException;
import com.api.notionary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfirmationTokenService confirmationTokenService;

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
                LocalDateTime.now().plusDays(1),
                user
        );

        confirmationTokenService.saveConfirmationToken(confirmationToken);
        return token;
    }

    @Transactional
    @PreAuthorize("#id == #currentUser.id or hasRole('ROLE_ADMIN')")
    public void deleteUserById(Long id, User currentUser) {
        User userToDelete = getUserEntityById(id);
        userRepository.delete(userToDelete);
        log.info("User with id {} was removed from repository by {}.", id, currentUser.getEmail());
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
