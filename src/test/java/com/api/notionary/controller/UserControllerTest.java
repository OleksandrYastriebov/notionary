package com.api.notionary.controller;

import com.api.notionary.dto.payload.request.user.ChangePasswordRequest;
import com.api.notionary.dto.payload.request.user.UpdateUserRequest;
import com.api.notionary.dto.user.UserProfileDto;
import com.api.notionary.entity.User;
import com.api.notionary.entity.UserRole;
import com.api.notionary.exception.EntityNotFoundException;
import com.api.notionary.exception.GlobalExceptionHandler;
import com.api.notionary.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        objectMapper = Jackson2ObjectMapperBuilder.json().build();

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        authenticatedUser = new User("John", "Doe", "john@notionary.app", "hashed",
                LocalDateTime.of(2024, 1, 1, 0, 0), UserRole.ROLE_USER);
        authenticatedUser.setId(10L);
        authenticatedUser.setEnabled(true);

        var auth = UsernamePasswordAuthenticationToken.authenticated(
                authenticatedUser, null, authenticatedUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserProfile_whenAuthenticated_shouldReturnProfileDto() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@notionary.app"));
    }

    @Test
    void deleteUser_whenOwner_shouldReturnOkWithMessage() throws Exception {
        doNothing().when(userService).deleteUserById(eq(10L), any(User.class));

        mockMvc.perform(delete("/api/v1/users/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User with id 10 was successfully removed."));

        verify(userService).deleteUserById(eq(10L), any(User.class));
    }

    @Test
    void deleteUser_whenForbidden_shouldReturn403() throws Exception {
        doThrow(new AccessDeniedException("Not your account"))
                .when(userService).deleteUserById(eq(99L), any(User.class));

        mockMvc.perform(delete("/api/v1/users/99"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_whenUserNotFound_shouldReturn404() throws Exception {
        doThrow(new EntityNotFoundException("User not found"))
                .when(userService).deleteUserById(eq(999L), any(User.class));

        mockMvc.perform(delete("/api/v1/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserProfile_whenValidRequest_shouldReturnUpdatedProfile() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("Johnny", "Smith", null);
        UserProfileDto updatedProfile = new UserProfileDto(
                10L, "Johnny", "Smith", "john@notionary.app", null,
                LocalDateTime.of(2024, 1, 1, 0, 0));
        when(userService.updateUser(any(User.class), any(UpdateUserRequest.class))).thenReturn(updatedProfile);

        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Johnny"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    void updateUserProfile_whenFirstNameEmptyString_shouldReturn400() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("", "Smith", null);

        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserProfile_whenAvatarUrlInvalid_shouldReturn400() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest(null, null, "not-a-url");

        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserProfile_whenBodyMissing_shouldReturn400() throws Exception {
        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_whenValidRequest_shouldReturnOkWithMessage() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("OldPass123", "NewPass456!");
        doNothing().when(userService).changePassword(any(User.class), any(ChangePasswordRequest.class));

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated successfully."));

        verify(userService).changePassword(any(User.class), any(ChangePasswordRequest.class));
    }

    @Test
    void changePassword_whenNewPasswordTooShort_shouldReturn400() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("OldPass123", "short");

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_whenCurrentPasswordBlank_shouldReturn400() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("", "NewPass456!");

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_whenBodyMissing_shouldReturn400() throws Exception {
        mockMvc.perform(patch("/api/v1/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
