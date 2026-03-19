package com.api.wishoria.service;

import com.api.wishoria.dto.payload.request.user.ChangePasswordRequest;
import com.api.wishoria.dto.payload.request.user.UpdateUserRequest;
import com.api.wishoria.dto.user.PublicUserDto;
import com.api.wishoria.dto.user.UserAutocompleteDto;
import com.api.wishoria.dto.user.UserProfileDto;
import com.api.wishoria.entity.ConfirmationToken;
import com.api.wishoria.entity.User;
import com.api.wishoria.exception.UserAlreadyExistsException;
import com.api.wishoria.exception.UserNotFoundException;
import com.api.wishoria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
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
        validateEmailIsUnique(user.getEmail());

        user.setPassword(passwordEncoder.encode(user.getPassword()));
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
        anonymizeUser(userToDelete);
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

    @Transactional
    public void changePassword(User currentUser, ChangePasswordRequest request) {
        User user = getUserEntityById(currentUser.getId());
        validateCurrentPassword(request.currentPassword(), user.getPassword());
        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }

    public List<PublicUserDto> searchPublicUsers(String query, User currentUser) {
        if (query == null || query.trim().length() < 2) {
            return Collections.emptyList();
        }

        Long excludeUserId = currentUser != null ? currentUser.getId() : null;

        List<User> users = userRepository.searchUsersByQuery(
                query.trim(),
                excludeUserId,
                PageRequest.of(0, 20)
        );

        return users.stream()
                .map(user -> new PublicUserDto(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getAvatarUrl(),
                        user.getProfileDescription()))
                .toList();
    }

    public List<UserAutocompleteDto> getUsersForAutocomplete(String query, User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("You need to be logged in to search users.");
        }

        if (query == null || query.trim().length() < 2) {
            return Collections.emptyList();
        }

        return userRepository.searchByEmailForAutocomplete(
                query.trim(),
                currentUser.getId(),
                PageRequest.of(0, 5)
        );
    }

    public PublicUserDto getPublicUserById(Long userId) {
        User user = getUserEntityById(userId);
        if (user.isPrivateProfile()) {
            throw new UserNotFoundException("User profile is private.");
        }
        return new PublicUserDto(user.getId(), user.getFirstName(), user.getLastName(), user.getAvatarUrl(), user.getProfileDescription());
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id %s can not be found.", userId)));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UserNotFoundException(String.format("User with email %s not found.", email)));
    }

    private void validateEmailIsUnique(String email) {
        if (userRepository.existsByEmail(email.toLowerCase().trim())) {
            throw new UserAlreadyExistsException("Email already taken.");
        }
    }

    private void validateCurrentPassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new IllegalArgumentException("Incorrect current password.");
        }
    }

    private void anonymizeUser(User user) {
        String randomHash = UUID.randomUUID().toString().substring(0, 8);
        user.setEmail("deleted_" + randomHash + "@wishoria.deleted");
        user.setFirstName("Deleted");
        user.setLastName("User");
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setEnabled(false);
        user.setDeleted(true);
    }

    private User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id %s can not be found.", id)));
    }

    private @NonNull ConfirmationToken buildConfirmationToken(User user, String token) {
        return new ConfirmationToken(
                token,
                Instant.now(),
                Instant.now().plus(Duration.ofDays(tokenExpirationDays)),
                user
        );
    }
}