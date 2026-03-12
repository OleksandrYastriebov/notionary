package com.api.notionary.controller;

import com.api.notionary.controller.docs.ApiForbiddenErrorDoc;
import com.api.notionary.controller.docs.ApiNotFoundErrorDoc;
import com.api.notionary.controller.docs.ApiUnauthorizedErrorDoc;
import com.api.notionary.dto.payload.request.wishlist.CreateWishlistRequest;
import com.api.notionary.dto.payload.request.wishlist.UpdateWishlistRequest;
import com.api.notionary.dto.wishlist.WishListContainerDto;
import com.api.notionary.dto.wishlist.WishListDto;
import com.api.notionary.dto.ApiResponseWrapper;
import com.api.notionary.entity.User;
import com.api.notionary.security.interceptor.RateLimitPlan;
import com.api.notionary.security.interceptor.RateLimited;
import com.api.notionary.service.WishListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Tag(name = "Wishlists", description = "Operations related to managing user wishlists")
@RequiredArgsConstructor
@RateLimited(action = RateLimitPlan.DEFAULT)
@RestController
@RequestMapping("/api/v1/wishlists")
public class WishlistController {

    private final WishListService wishlistService;

    @Operation(summary = "Get all wishlists", description = "Retrieves all wishlists belonging to the authenticated user.")
    @ApiUnauthorizedErrorDoc
    @GetMapping
    public ResponseEntity<WishListContainerDto> getWishlists(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok().body(wishlistService.getWishlistsForUser(user));
    }

    @Operation(summary = "Get a specific wishlist", description = "Retrieves details of a specific wishlist by its ID.")
    @ApiNotFoundErrorDoc
    @ApiForbiddenErrorDoc
    @GetMapping("/{wishlistId}")
    public ResponseEntity<WishListDto> getWishlist(@PathVariable String wishlistId, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(wishlistService.findWishlistById(wishlistId, user));
    }

    @Operation(summary = "Create a new wishlist", description = "Creates a new wishlist for the authenticated user.")
    @ApiUnauthorizedErrorDoc
    @RateLimited(action = RateLimitPlan.MUTATION)
    @PostMapping
    public ResponseEntity<WishListDto> createWishList(@Valid @RequestBody CreateWishlistRequest createWishlistRequest,
                                                      @AuthenticationPrincipal User user) {
        WishListDto wishlist = wishlistService.createWishlist(createWishlistRequest, user);
        return ResponseEntity.created(URI.create("/api/v1/wishlists/" + wishlist.id())).body(wishlist);
    }

    @Operation(summary = "Delete a wishlist", description = "Permanently deletes a wishlist and all its items.")
    @ApiUnauthorizedErrorDoc
    @ApiNotFoundErrorDoc
    @ApiForbiddenErrorDoc
    @RateLimited(action = RateLimitPlan.MUTATION)
    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<ApiResponseWrapper> deleteWishList(@PathVariable String wishlistId, @AuthenticationPrincipal User user) {
        wishlistService.deleteWishList(wishlistId, user);
        return ResponseEntity.ok(new ApiResponseWrapper(String.format("Wishlist with id %s was successfully removed from database", wishlistId)));
    }

    @Operation(summary = "Update a wishlist", description = "Updates the title, visibility, or image of an existing wishlist.")
    @ApiUnauthorizedErrorDoc
    @ApiNotFoundErrorDoc
    @ApiForbiddenErrorDoc
    @RateLimited(action = RateLimitPlan.MUTATION)
    @PatchMapping("/{wishlistId}")
    public ResponseEntity<ApiResponseWrapper> updateWishlist(@PathVariable String wishlistId,
                                                             @Valid @RequestBody UpdateWishlistRequest updateWishlistRequest,
                                                             @AuthenticationPrincipal User user) {

        WishListDto wishListDto = wishlistService.updateWishlist(wishlistId, updateWishlistRequest, user);
        return ResponseEntity.ok(new ApiResponseWrapper(String.format("Updated Wishlist: %s", wishListDto)));
    }
}
