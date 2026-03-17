package com.api.wishoria.controller;

import com.api.wishoria.dto.ApiResponseWrapper;
import com.api.wishoria.dto.access.AccessesContainerDto;
import com.api.wishoria.dto.payload.request.wishlist.RevokeAccessRequest;
import com.api.wishoria.dto.payload.request.wishlist.ShareWishListRequest;
import com.api.wishoria.entity.User;
import com.api.wishoria.entity.UserRole;
import com.api.wishoria.exception.EntityNotFoundException;
import com.api.wishoria.exception.GlobalExceptionHandler;
import com.api.wishoria.service.WishListAccessService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WishListAccessControllerTest {

    @Mock
    private WishListAccessService wishlistAccessService;

    @InjectMocks
    private WishListAccessController wishListAccessController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/wishlists/wl-id-1/access";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(wishListAccessController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();

        User authenticatedUser = new User("Owner", "User", "owner@wishoria.app", "hashed",
                Instant.now(), UserRole.ROLE_USER);
        authenticatedUser.setId(1L);
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
    void getAllGrantedAccesses_whenOwner_shouldReturnEmailList() throws Exception {
        AccessesContainerDto container = new AccessesContainerDto(
                List.of("friend@wishoria.app", "colleague@wishoria.app"));
        when(wishlistAccessService.getAllGrantedEmailsForWishlist(eq("wl-id-1"), any(User.class)))
                .thenReturn(container);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emails", hasSize(2)))
                .andExpect(jsonPath("$.emails[0]").value("friend@wishoria.app"))
                .andExpect(jsonPath("$.emails[1]").value("colleague@wishoria.app"));
    }

    @Test
    void getAllGrantedAccesses_whenNoAccessGranted_shouldReturnEmptyList() throws Exception {
        when(wishlistAccessService.getAllGrantedEmailsForWishlist(eq("wl-id-1"), any(User.class)))
                .thenReturn(new AccessesContainerDto(List.of()));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emails", hasSize(0)));
    }

    @Test
    void getAllGrantedAccesses_whenNotOwner_shouldReturn403() throws Exception {
        when(wishlistAccessService.getAllGrantedEmailsForWishlist(eq("wl-id-1"), any(User.class)))
                .thenThrow(new AccessDeniedException("Only the owner can view granted accesses"));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllGrantedAccesses_whenWishlistNotFound_shouldReturn404() throws Exception {
        when(wishlistAccessService.getAllGrantedEmailsForWishlist(eq("wl-id-1"), any(User.class)))
                .thenThrow(new EntityNotFoundException("Wishlist not found"));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isNotFound());
    }

    @Test
    void grantAccess_whenValidRequest_shouldReturnOkWithMessage() throws Exception {
        ShareWishListRequest request = new ShareWishListRequest("friend@wishoria.app");
        ApiResponseWrapper serviceResponse = new ApiResponseWrapper("Access granted to friend@wishoria.app");
        when(wishlistAccessService.grantAccess(eq("wl-id-1"), any(ShareWishListRequest.class), any(User.class)))
                .thenReturn(serviceResponse);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Access granted to friend@wishoria.app"));

        verify(wishlistAccessService).grantAccess(eq("wl-id-1"), any(ShareWishListRequest.class), any(User.class));
    }

    @Test
    void grantAccess_whenEmailBlank_shouldReturn400() throws Exception {
        ShareWishListRequest request = new ShareWishListRequest("");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void grantAccess_whenEmailInvalidFormat_shouldReturn400() throws Exception {
        ShareWishListRequest request = new ShareWishListRequest("not-an-email");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void grantAccess_whenNotOwner_shouldReturn403() throws Exception {
        ShareWishListRequest request = new ShareWishListRequest("friend@wishoria.app");
        when(wishlistAccessService.grantAccess(eq("wl-id-1"), any(), any(User.class)))
                .thenThrow(new AccessDeniedException("Only the wishlist owner can grant access"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void grantAccess_whenWishlistNotFound_shouldReturn404() throws Exception {
        ShareWishListRequest request = new ShareWishListRequest("friend@wishoria.app");
        when(wishlistAccessService.grantAccess(eq("wl-id-1"), any(), any(User.class)))
                .thenThrow(new EntityNotFoundException("Wishlist not found"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void grantAccess_whenBodyMissing_shouldReturn400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void revokeAccess_whenValidRequest_shouldReturnOkWithMessage() throws Exception {
        RevokeAccessRequest request = new RevokeAccessRequest("friend@wishoria.app");
        ApiResponseWrapper serviceResponse = new ApiResponseWrapper("Access revoked for friend@wishoria.app");
        when(wishlistAccessService.revokeAccess(eq("wl-id-1"), any(RevokeAccessRequest.class), any(User.class)))
                .thenReturn(serviceResponse);

        mockMvc.perform(delete(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Access revoked for friend@wishoria.app"));

        verify(wishlistAccessService).revokeAccess(eq("wl-id-1"), any(RevokeAccessRequest.class), any(User.class));
    }

    @Test
    void revokeAccess_whenEmailBlank_shouldReturn400() throws Exception {
        RevokeAccessRequest request = new RevokeAccessRequest("");

        mockMvc.perform(delete(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void revokeAccess_whenEmailInvalidFormat_shouldReturn400() throws Exception {
        RevokeAccessRequest request = new RevokeAccessRequest("not-valid-email");

        mockMvc.perform(delete(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void revokeAccess_whenNotOwner_shouldReturn403() throws Exception {
        RevokeAccessRequest request = new RevokeAccessRequest("friend@wishoria.app");
        when(wishlistAccessService.revokeAccess(eq("wl-id-1"), any(), any(User.class)))
                .thenThrow(new AccessDeniedException("Only the wishlist owner can revoke access"));

        mockMvc.perform(delete(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void revokeAccess_whenBodyMissing_shouldReturn400() throws Exception {
        mockMvc.perform(delete(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
