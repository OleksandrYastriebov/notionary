package com.api.notionary.controller;

import com.api.notionary.dto.payload.request.wishlistitem.CreateWishListItemRequest;
import com.api.notionary.dto.payload.request.wishlistitem.UpdateWishListItemRequest;
import com.api.notionary.dto.payload.request.wishlistitem.WishlistItemIsCheckedRequest;
import com.api.notionary.dto.wishlistitem.WishListItemContainerDto;
import com.api.notionary.dto.wishlistitem.WishListItemDto;
import com.api.notionary.entity.User;
import com.api.notionary.entity.UserRole;
import com.api.notionary.exception.EntityNotFoundException;
import com.api.notionary.exception.GlobalExceptionHandler;
import com.api.notionary.service.WishListItemService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
class WishListItemControllerTest {

    @Mock
    private WishListItemService wishListItemService;

    @InjectMocks
    private WishListItemController wishListItemController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private WishListItemDto sampleItemDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(wishListItemController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        User authenticatedUser = new User("Jane", "Doe", "jane@notionary.app", "hashed",
                LocalDateTime.of(2024, 1, 1, 0, 0), UserRole.ROLE_USER);
        authenticatedUser.setId(2L);
        authenticatedUser.setEnabled(true);

        var auth = UsernamePasswordAuthenticationToken.authenticated(
                authenticatedUser, null, authenticatedUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        sampleItemDto = new WishListItemDto("item-id-1", "wl-id-1", "PlayStation 5",
                "https://store.sony.com/ps5", new BigDecimal("499.99"),
                "Disc edition", "https://example.com/ps5.jpg", false,
                LocalDateTime.of(2024, 6, 1, 10, 0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createWishListItem_whenValidRequest_shouldReturn201WithLocationHeader() throws Exception {
        CreateWishListItemRequest request = new CreateWishListItemRequest(
                "PlayStation 5", "https://store.sony.com/ps5", new BigDecimal("499.99"),
                "Disc edition", "https://example.com/ps5.jpg");
        when(wishListItemService.createWishListItem(eq("wl-id-1"), any(CreateWishListItemRequest.class), any(User.class)))
                .thenReturn(sampleItemDto);

        mockMvc.perform(post("/api/v1/wishlists/wl-id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/wishlists/wl-id-1/wishes/item-id-1"))
                .andExpect(jsonPath("$.id").value("item-id-1"))
                .andExpect(jsonPath("$.title").value("PlayStation 5"))
                .andExpect(jsonPath("$.price").value(499.99));
    }

    @Test
    void createWishListItem_whenTitleBlank_shouldReturn400() throws Exception {
        CreateWishListItemRequest request = new CreateWishListItemRequest(
                "", null, null, null, null);

        mockMvc.perform(post("/api/v1/wishlists/wl-id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWishListItem_whenItemUrlInvalid_shouldReturn400() throws Exception {
        CreateWishListItemRequest request = new CreateWishListItemRequest(
                "Item", "not-a-url", null, null, null);

        mockMvc.perform(post("/api/v1/wishlists/wl-id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWishListItem_whenWishlistNotFound_shouldReturn404() throws Exception {
        CreateWishListItemRequest request = new CreateWishListItemRequest(
                "Item", null, null, null, null);
        when(wishListItemService.createWishListItem(eq("ghost"), any(), any()))
                .thenThrow(new EntityNotFoundException("Wishlist not found"));

        mockMvc.perform(post("/api/v1/wishlists/ghost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWishListItem_whenForbidden_shouldReturn403() throws Exception {
        CreateWishListItemRequest request = new CreateWishListItemRequest(
                "Item", null, null, null, null);
        when(wishListItemService.createWishListItem(eq("wl-id-1"), any(), any()))
                .thenThrow(new AccessDeniedException("Not allowed"));

        mockMvc.perform(post("/api/v1/wishlists/wl-id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllWishlistItems_whenListExists_shouldReturnContainerDto() throws Exception {
        WishListItemContainerDto container = new WishListItemContainerDto(List.of(sampleItemDto));
        when(wishListItemService.findAllWishListItemsForWishList(eq("wl-id-1"), any()))
                .thenReturn(container);

        mockMvc.perform(get("/api/v1/wishlists/wl-id-1/wishes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wishListItems", hasSize(1)))
                .andExpect(jsonPath("$.wishListItems[0].id").value("item-id-1"))
                .andExpect(jsonPath("$.wishListItems[0].title").value("PlayStation 5"));
    }

    @Test
    void getAllWishlistItems_whenWishlistNotFound_shouldReturn404() throws Exception {
        when(wishListItemService.findAllWishListItemsForWishList(eq("nonexistent"), any()))
                .thenThrow(new EntityNotFoundException("Wishlist not found"));

        mockMvc.perform(get("/api/v1/wishlists/nonexistent/wishes"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllWishlistItems_whenEmptyList_shouldReturnEmptyContainer() throws Exception {
        when(wishListItemService.findAllWishListItemsForWishList(eq("wl-id-1"), any()))
                .thenReturn(new WishListItemContainerDto(List.of()));

        mockMvc.perform(get("/api/v1/wishlists/wl-id-1/wishes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wishListItems", hasSize(0)));
    }

    @Test
    void getWishListItem_whenExists_shouldReturnItemDto() throws Exception {
        when(wishListItemService.findWishlistItemByIdAndWishlistId(eq("wl-id-1"), eq("item-id-1"), any()))
                .thenReturn(sampleItemDto);

        mockMvc.perform(get("/api/v1/wishlists/wl-id-1/wishes/item-id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("item-id-1"))
                .andExpect(jsonPath("$.wishListId").value("wl-id-1"))
                .andExpect(jsonPath("$.price").value(499.99))
                .andExpect(jsonPath("$.isChecked").value(false));
    }

    @Test
    void getWishListItem_whenNotFound_shouldReturn404() throws Exception {
        when(wishListItemService.findWishlistItemByIdAndWishlistId(eq("wl-id-1"), eq("ghost"), any()))
                .thenThrow(new EntityNotFoundException("Item not found"));

        mockMvc.perform(get("/api/v1/wishlists/wl-id-1/wishes/ghost"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteWishListItem_whenOwner_shouldReturnOkWithMessage() throws Exception {
        doNothing().when(wishListItemService).deleteWishlistItem(eq("wl-id-1"), eq("item-id-1"), any(User.class));

        mockMvc.perform(delete("/api/v1/wishlists/wl-id-1/wishes/item-id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "Wishlist item with id: item-id-1 was successfully removed from the wishlist: wl-id-1"));

        verify(wishListItemService).deleteWishlistItem(eq("wl-id-1"), eq("item-id-1"), any(User.class));
    }

    @Test
    void deleteWishListItem_whenForbidden_shouldReturn403() throws Exception {
        doThrow(new AccessDeniedException("Not the owner"))
                .when(wishListItemService).deleteWishlistItem(eq("wl-id-1"), eq("item-id-1"), any(User.class));

        mockMvc.perform(delete("/api/v1/wishlists/wl-id-1/wishes/item-id-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteWishListItem_whenItemNotFound_shouldReturn404() throws Exception {
        doThrow(new EntityNotFoundException("Item not found"))
                .when(wishListItemService).deleteWishlistItem(eq("wl-id-1"), eq("ghost"), any(User.class));

        mockMvc.perform(delete("/api/v1/wishlists/wl-id-1/wishes/ghost"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateWishlistItem_whenValidRequest_shouldReturnUpdatedDto() throws Exception {
        UpdateWishListItemRequest request = new UpdateWishListItemRequest(
                "PS5 Slim", null, null, null, null, null);
        WishListItemDto updatedItem = new WishListItemDto("item-id-1", "wl-id-1", "PS5 Slim",
                null, null, null, null, false, LocalDateTime.now());
        when(wishListItemService.updateWishlistItem(any(UpdateWishListItemRequest.class),
                eq("wl-id-1"), eq("item-id-1"), any(User.class))).thenReturn(updatedItem);

        mockMvc.perform(patch("/api/v1/wishlists/wl-id-1/wishes/item-id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("item-id-1"))
                .andExpect(jsonPath("$.title").value("PS5 Slim"));
    }

    @Test
    void updateWishlistItem_whenTitleEmptyString_shouldReturn400() throws Exception {
        UpdateWishListItemRequest request = new UpdateWishListItemRequest(
                "", null, null, null, null, null);

        mockMvc.perform(patch("/api/v1/wishlists/wl-id-1/wishes/item-id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateWishlistItem_whenUrlInvalid_shouldReturn400() throws Exception {
        UpdateWishListItemRequest request = new UpdateWishListItemRequest(
                null, "not-a-url", null, null, null, null);

        mockMvc.perform(patch("/api/v1/wishlists/wl-id-1/wishes/item-id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateWishlistItem_whenForbidden_shouldReturn403() throws Exception {
        UpdateWishListItemRequest request = new UpdateWishListItemRequest(
                "PS5 Slim", null, null, null, null, null);
        when(wishListItemService.updateWishlistItem(any(), eq("wl-id-1"), eq("item-id-1"), any()))
                .thenThrow(new AccessDeniedException("Not the owner"));

        mockMvc.perform(patch("/api/v1/wishlists/wl-id-1/wishes/item-id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void toggleItemCheck_whenValidRequest_shouldReturnOkMessage() throws Exception {
        WishlistItemIsCheckedRequest request = new WishlistItemIsCheckedRequest(true);
        doNothing().when(wishListItemService).toggleIsChecked(
                eq("wl-id-1"), eq("item-id-1"), any(WishlistItemIsCheckedRequest.class), any());

        mockMvc.perform(patch("/api/v1/wishlists/wl-id-1/wishes/item-id-1/checked")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Item status updated successfully"));
    }

    @Test
    void toggleItemCheck_whenIsCheckedNull_shouldReturn400() throws Exception {
        String body = "{\"isChecked\": null}";

        mockMvc.perform(patch("/api/v1/wishlists/wl-id-1/wishes/item-id-1/checked")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void toggleItemCheck_whenWishlistItemNotFound_shouldReturn404() throws Exception {
        WishlistItemIsCheckedRequest request = new WishlistItemIsCheckedRequest(true);
        doThrow(new EntityNotFoundException("Item not found"))
                .when(wishListItemService).toggleIsChecked(eq("wl-id-1"), eq("ghost"), any(), any());

        mockMvc.perform(patch("/api/v1/wishlists/wl-id-1/wishes/ghost/checked")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void toggleItemCheck_whenBodyMissing_shouldReturn400() throws Exception {
        mockMvc.perform(patch("/api/v1/wishlists/wl-id-1/wishes/item-id-1/checked")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
