package com.api.notionary.controller;

import com.api.notionary.dto.payload.request.wishlist.CreateWishlistRequest;
import com.api.notionary.dto.payload.request.wishlist.UpdateWishlistRequest;
import com.api.notionary.dto.wishlist.WishListContainerDto;
import com.api.notionary.dto.wishlist.WishListDto;
import com.api.notionary.entity.User;
import com.api.notionary.entity.UserRole;
import com.api.notionary.exception.EntityNotFoundException;
import com.api.notionary.exception.GlobalExceptionHandler;
import com.api.notionary.service.WishListService;
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
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WishlistControllerTest {

    @Mock
    private WishListService wishlistService;

    @InjectMocks
    private WishlistController wishlistController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private WishListDto sampleWishListDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(wishlistController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        User authenticatedUser = new User("John", "Doe", "john@notionary.app", "hashed",
                Instant.now(), UserRole.ROLE_USER);
        authenticatedUser.setId(1L);
        authenticatedUser.setEnabled(true);

        var auth = UsernamePasswordAuthenticationToken.authenticated(
                authenticatedUser, null, authenticatedUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        sampleWishListDto = new WishListDto("wl-id-1", 1L, List.of(), "Birthday Wishes",
                false, null, Instant.now());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getWishlists_whenAuthenticated_shouldReturnContainerDtoWithWishlists() throws Exception {
        WishListContainerDto container = new WishListContainerDto(List.of(sampleWishListDto));
        when(wishlistService.getWishlistsForUser(any(User.class))).thenReturn(container);

        mockMvc.perform(get("/api/v1/wishlists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wishLists", hasSize(1)))
                .andExpect(jsonPath("$.wishLists[0].id").value("wl-id-1"))
                .andExpect(jsonPath("$.wishLists[0].title").value("Birthday Wishes"));
    }

    @Test
    void getWishlists_whenEmptyList_shouldReturnEmptyContainer() throws Exception {
        when(wishlistService.getWishlistsForUser(any(User.class)))
                .thenReturn(new WishListContainerDto(List.of()));

        mockMvc.perform(get("/api/v1/wishlists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wishLists", hasSize(0)));
    }

    @Test
    void getWishlist_whenAuthenticated_shouldReturnWishlistDto() throws Exception {
        when(wishlistService.findWishlistById(eq("wl-id-1"), any(User.class))).thenReturn(sampleWishListDto);

        mockMvc.perform(get("/api/v1/wishlists/wl-id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("wl-id-1"))
                .andExpect(jsonPath("$.title").value("Birthday Wishes"))
                .andExpect(jsonPath("$.isPublic").value(false));
    }

    @Test
    void getWishlist_whenNotFound_shouldReturn404() throws Exception {
        when(wishlistService.findWishlistById(eq("nonexistent"), any()))
                .thenThrow(new EntityNotFoundException("Wishlist not found"));

        mockMvc.perform(get("/api/v1/wishlists/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getWishlist_whenForbidden_shouldReturn403() throws Exception {
        when(wishlistService.findWishlistById(eq("private-wl"), any()))
                .thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(get("/api/v1/wishlists/private-wl"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createWishList_whenValidRequest_shouldReturn201WithLocationHeader() throws Exception {
        CreateWishlistRequest request = new CreateWishlistRequest("New Wishlist", true, null);
        when(wishlistService.createWishlist(any(CreateWishlistRequest.class), any(User.class)))
                .thenReturn(sampleWishListDto);

        mockMvc.perform(post("/api/v1/wishlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/wishlists/wl-id-1"))
                .andExpect(jsonPath("$.id").value("wl-id-1"))
                .andExpect(jsonPath("$.title").value("Birthday Wishes"));
    }

    @Test
    void createWishList_whenTitleBlank_shouldReturn400() throws Exception {
        CreateWishlistRequest request = new CreateWishlistRequest("", null, null);

        mockMvc.perform(post("/api/v1/wishlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWishList_whenTitleTooLong_shouldReturn400() throws Exception {
        String longTitle = "A".repeat(101);
        CreateWishlistRequest request = new CreateWishlistRequest(longTitle, null, null);

        mockMvc.perform(post("/api/v1/wishlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWishList_whenImageUrlInvalid_shouldReturn400() throws Exception {
        CreateWishlistRequest request = new CreateWishlistRequest("My List", null, "not-a-valid-url");

        mockMvc.perform(post("/api/v1/wishlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWishList_whenBodyMissing_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/wishlists")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteWishList_whenOwner_shouldReturnOkWithMessage() throws Exception {
        doNothing().when(wishlistService).deleteWishList(eq("wl-id-1"), any(User.class));

        mockMvc.perform(delete("/api/v1/wishlists/wl-id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Wishlist with id wl-id-1 was successfully deleted."));

        verify(wishlistService).deleteWishList(eq("wl-id-1"), any(User.class));
    }

    @Test
    void deleteWishList_whenForbidden_shouldReturn403() throws Exception {
        doThrow(new AccessDeniedException("Not your wishlist"))
                .when(wishlistService).deleteWishList(eq("other-wl"), any(User.class));

        mockMvc.perform(delete("/api/v1/wishlists/other-wl"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteWishList_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new EntityNotFoundException("Wishlist not found"))
                .when(wishlistService).deleteWishList(eq("ghost"), any(User.class));

        mockMvc.perform(delete("/api/v1/wishlists/ghost"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateWishlist_whenValidRequest_shouldReturnOkWithMessage() throws Exception {
        UpdateWishlistRequest request = new UpdateWishlistRequest("Updated Title", true, null);
        WishListDto updatedDto = new WishListDto("wl-id-1", 1L, List.of(), "Updated Title",
                true, null, Instant.now());
        when(wishlistService.updateWishlist(eq("wl-id-1"), any(UpdateWishlistRequest.class), any(User.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(patch("/api/v1/wishlists/wl-id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updateWishlist_whenTitleEmptyString_shouldReturn400() throws Exception {
        UpdateWishlistRequest request = new UpdateWishlistRequest("", null, null);

        mockMvc.perform(patch("/api/v1/wishlists/wl-id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateWishlist_whenImageUrlInvalid_shouldReturn400() throws Exception {
        UpdateWishlistRequest request = new UpdateWishlistRequest(null, null, "not-a-url");

        mockMvc.perform(patch("/api/v1/wishlists/wl-id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateWishlist_whenForbidden_shouldReturn403() throws Exception {
        UpdateWishlistRequest request = new UpdateWishlistRequest("Title", null, null);
        when(wishlistService.updateWishlist(eq("other-wl"), any(), any()))
                .thenThrow(new AccessDeniedException("Not your wishlist"));

        mockMvc.perform(patch("/api/v1/wishlists/other-wl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateWishlist_whenNotFound_shouldReturn404() throws Exception {
        UpdateWishlistRequest request = new UpdateWishlistRequest("Title", null, null);
        when(wishlistService.updateWishlist(eq("ghost"), any(), any()))
                .thenThrow(new EntityNotFoundException("Wishlist not found"));

        mockMvc.perform(patch("/api/v1/wishlists/ghost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
