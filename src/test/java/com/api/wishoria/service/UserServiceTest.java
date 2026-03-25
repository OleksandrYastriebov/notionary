package com.api.wishoria.service;

import com.api.wishoria.dto.user.request.ChangePasswordRequest;
import com.api.wishoria.dto.user.request.UpdateUserRequest;
import com.api.wishoria.dto.user.PublicUserDto;
import com.api.wishoria.dto.user.UserProfileDto;
import com.api.wishoria.entity.ConfirmationToken;
import com.api.wishoria.entity.User;
import com.api.wishoria.entity.UserRole;
import com.api.wishoria.exception.UserAlreadyExistsException;
import com.api.wishoria.exception.UserNotFoundException;
import com.api.wishoria.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final int TOKEN_EXPIRATION_DAYS = 7;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ConfirmationTokenService confirmationTokenService;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "tokenExpirationDays", TOKEN_EXPIRATION_DAYS);
        user = new User("John", "Doe", "john@example.com", "rawPassword",
                Instant.now(), UserRole.ROLE_USER);
        user.setId(1L);
        user.setEnabled(true);
    }

    @Test
    void signUpUser_shouldEncodePasswordAndSaveUser_whenEmailNotTaken() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        String token = userService.signUpUser(user);

        verify(userRepository).existsByEmail("john@example.com");
        verify(passwordEncoder).encode("rawPassword");
        assertThat(user.getPassword()).isEqualTo("encodedPassword");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("john@example.com");
        verify(confirmationTokenService).deleteTokensByUserIds(List.of(1L));
        verify(confirmationTokenService).saveConfirmationToken(any(ConfirmationToken.class));
        assertThat(token).isNotNull();
    }

    @Test
    void signUpUser_shouldNormalizeEmailToLowerCase() {
        user.setEmail("JOHN@Example.COM");
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.signUpUser(user);

        verify(userRepository).existsByEmail("john@example.com");
    }

    @Test
    void signUpUser_shouldThrow_whenEmailAlreadyTaken() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.signUpUser(user))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Email already taken.");
        verify(userRepository, never()).save(any());
    }

    @Test
    void generateNewConfirmationToken_shouldDeleteOldTokensAndSaveNew() {
        String token = userService.generateNewConfirmationToken(user);

        verify(confirmationTokenService).deleteTokensByUserIds(List.of(1L));
        verify(confirmationTokenService).saveConfirmationToken(any(ConfirmationToken.class));
        assertThat(token).isNotNull();
    }

    @Test
    void deleteUserById_shouldSoftDeleteAndInvalidateTokens() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUserById(1L, user);

        verify(userRepository).findById(1L);
        assertThat(user.getEmail()).startsWith("deleted_");
        assertThat(user.getEmail()).endsWith("@wishoria.deleted");
        assertThat(user.getFirstName()).isEqualTo("Deleted");
        assertThat(user.getLastName()).isEqualTo("User");
        assertThat(user.isEnabled()).isFalse();
        assertThat(user.isDeleted()).isTrue();
        verify(passwordEncoder).encode(any(String.class));
        verify(refreshTokenService).deleteByUserId(1L);
    }

    @Test
    void deleteUserById_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUserById(999L, user))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void cleanupUnverifiedUsers_shouldDoNothing_whenNoExpiredUsers() {
        when(userRepository.findIdsOfExpiredAndDisabledUsers()).thenReturn(List.of());

        userService.cleanupUnverifiedUsers();

        verify(confirmationTokenService, never()).deleteTokensByUserIds(any());
        verify(userRepository, never()).bulkDeleteByIds(any());
    }

    @Test
    void cleanupUnverifiedUsers_shouldDeleteTokensAndUsers_whenExpiredUsersExist() {
        List<Long> ids = List.of(10L, 20L);
        when(userRepository.findIdsOfExpiredAndDisabledUsers()).thenReturn(ids);

        userService.cleanupUnverifiedUsers();

        verify(confirmationTokenService).deleteTokensByUserIds(ids);
        verify(userRepository).bulkDeleteByIds(ids);
    }

    @Test
    void updateUser_shouldApplyRequestAndReturnDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UpdateUserRequest request = new UpdateUserRequest("Jane", "Smith", "https://example.com/avatar.jpg", null, null, null);

        UserProfileDto result = userService.updateUser(user, request);

        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
        assertThat(result).isNotNull();
        assertThat(result.firstName()).isEqualTo("Jane");
    }

    @Test
    void changePassword_shouldUpdatePassword_whenCurrentPasswordMatches() {
        user.setPassword("encodedCurrent");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current", "encodedCurrent")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNew");
        ChangePasswordRequest request = new ChangePasswordRequest("current", "newPassword");

        userService.changePassword(user, request);

        verify(passwordEncoder).matches("current", "encodedCurrent");
        verify(passwordEncoder).encode("newPassword");
        assertThat(user.getPassword()).isEqualTo("encodedNew");
    }

    @Test
    void changePassword_shouldThrow_whenCurrentPasswordWrong() {
        user.setPassword("encodedCurrent");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedCurrent")).thenReturn(false);
        ChangePasswordRequest request = new ChangePasswordRequest("wrong", "newPassword");

        assertThatThrownBy(() -> userService.changePassword(user, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Incorrect current password.");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void getUserByEmail_shouldReturnUser_whenFound() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail("john@example.com");

        assertThat(result).isSameAs(user);
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void getUserByEmail_shouldNormalizeEmail() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        userService.getUserByEmail("  JOHN@Example.COM  ");

        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void getUserByEmail_shouldThrow_whenNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail("missing@example.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("missing@example.com");
    }

    @Test
    void searchPublicUsers_whenPrivateProfile_shouldNotBeIncluded() {
        when(userRepository.searchUsersByQuery(any(), any(), any())).thenReturn(List.of());

        List<PublicUserDto> result = userService.searchPublicUsers("john", user);

        verify(userRepository).searchUsersByQuery(any(), any(), any());
        assertThat(result).isEmpty();
    }

    @Test
    void getPublicUserById_whenProfileIsPrivate_shouldThrowUserNotFoundException() {
        user.setPrivateProfile(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.getPublicUserById(1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User profile is private.");
    }

    @Test
    void updateUser_whenProfileDescriptionProvided_shouldUpdateIt() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UpdateUserRequest request = new UpdateUserRequest(null, null, null, "I love coffee and tech", null, null);

        UserProfileDto result = userService.updateUser(user, request);

        assertThat(user.getProfileDescription()).isEqualTo("I love coffee and tech");
        assertThat(result).isNotNull();
    }

    @Test
    void updateUser_whenIsPrivateSet_shouldUpdatePrivateProfile() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, true, null);

        userService.updateUser(user, request);

        assertThat(user.isPrivateProfile()).isTrue();
    }

    @Test
    void getUserById_whenFound_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertThat(result).isSameAs(user);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_whenNotFound_shouldThrowUserNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }
}
