package com.api.wishoria.controller;

import com.api.wishoria.dto.payload.request.user.ChangePasswordRequest;
import com.api.wishoria.dto.payload.request.user.UpdateUserRequest;
import com.api.wishoria.dto.user.UserProfileDto;
import com.api.wishoria.entity.User;
import com.api.wishoria.entity.UserRole;
import com.api.wishoria.exception.EntityNotFoundException;
import com.api.wishoria.exception.GlobalExceptionHandler;
import com.api.wishoria.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

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

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        User authenticatedUser = new User("John", "Doe", "john@wishoria.app", "hashed",
                Instant.now(), UserRole.ROLE_USER);
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
                .andExpect(jsonPath("$.email").value("john@wishoria.app"));
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
        UpdateUserRequest request = new UpdateUserRequest("Johnny", "Smith", null, null, null);
        UserProfileDto updatedProfile = new UserProfileDto(
                10L, "Johnny", "Smith", "john@wishoria.app", null,
                Instant.now(), null, false);
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
        UpdateUserRequest request = new UpdateUserRequest("", "Smith", null, null, null);

        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserProfile_whenAvatarUrlInvalid_shouldReturn400() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest(null, null, "not-a-url", null, null);

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
