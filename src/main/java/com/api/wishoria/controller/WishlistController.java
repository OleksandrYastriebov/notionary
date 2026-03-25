package com.api.wishoria.controller;

import com.api.wishoria.controller.docs.ApiForbiddenErrorDoc;
import com.api.wishoria.controller.docs.ApiNotFoundErrorDoc;
import com.api.wishoria.controller.docs.ApiUnauthorizedErrorDoc;
import com.api.wishoria.dto.PagedResponse;
import com.api.wishoria.dto.wishlist.request.CreateWishlistRequest;
import com.api.wishoria.dto.wishlist.request.UpdateWishlistRequest;
import com.api.wishoria.dto.wishlist.WishListDto;
import com.api.wishoria.dto.ApiResponseWrapper;
import com.api.wishoria.entity.User;
import com.api.wishoria.security.interceptor.RateLimitPlan;
import com.api.wishoria.security.interceptor.RateLimited;
import com.api.wishoria.service.WishListService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Tag(name = "Wishlists", description = "Operations related to managing user wishlists")
@RequiredArgsConstructor
@RateLimited(action = RateLimitPlan.DEFAULT)
@RestController
@RequestMapping("/api/v1/wishlists")
public class WishlistController {

    private final WishListService wishlistService;

    @Operation(summary = "Get paginated wishlists", description = "Retrieves a page of wishlists belonging to the authenticated user.")
    @ApiUnauthorizedErrorDoc
    @GetMapping
    public ResponseEntity<PagedResponse<WishListDto>> getWishlists(@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "12") int size,
                                                                   @AuthenticationPrincipal User user) {
        return ResponseEntity.ok().body(wishlistService.getWishlistsForUser(user, page, size));
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
        return ResponseEntity.ok(new ApiResponseWrapper(String.format("Wishlist with id %s was successfully deleted.", wishlistId)));
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
