package com.api.notionary.service;

import com.api.notionary.dto.payload.request.user.UpdateUserRequest;
import com.api.notionary.dto.user.UserProfileDto;
import com.api.notionary.entity.ConfirmationToken;
import com.api.notionary.entity.User;
import com.api.notionary.exception.UserAlreadyExistsException;
import com.api.notionary.exception.UserNotFoundException;
import com.api.notionary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
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
@Transactional(readOnly = true)
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
        return generateNewConfirmationToken(user);
    }

    @Transactional
    public String generateNewConfirmationToken(User user) {
        confirmationTokenService.deleteTokensByUserIds(List.of(user.getId()));

        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = buildConfirmationToken(user, token);

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

    @Transactional
    public UserProfileDto updateUser(User currentUser, UpdateUserRequest request) {
        User user = getUserEntityById(currentUser.getId());
        request.updateEntity(user);
        return new UserProfileDto(user);
    }


    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with email %s not found.", email)));
    }

    private User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id %s can not be found.", id)));
    }

    private @NonNull ConfirmationToken buildConfirmationToken(User user, String token) {
        return new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(tokenExpirationDays),
                user
        );
    }

}
