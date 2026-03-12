package com.api.notionary.controller;

import com.api.notionary.controller.docs.ApiForbiddenErrorDoc;
import com.api.notionary.controller.docs.ApiNotFoundErrorDoc;
import com.api.notionary.controller.docs.ApiUnauthorizedErrorDoc;
import com.api.notionary.dto.payload.request.wishlistitem.CreateWishListItemRequest;
import com.api.notionary.dto.payload.request.wishlistitem.UpdateWishListItemRequest;
import com.api.notionary.dto.payload.request.wishlistitem.WishlistItemIsCheckedRequest;
import com.api.notionary.dto.wishlistitem.WishListItemContainerDto;
import com.api.notionary.dto.wishlistitem.WishListItemDto;
import com.api.notionary.dto.ApiResponseWrapper;
import com.api.notionary.entity.User;
import com.api.notionary.security.interceptor.RateLimitPlan;
import com.api.notionary.security.interceptor.RateLimited;
import com.api.notionary.service.WishListItemService;
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

@Tag(name = "Wishlist Items", description = "Operations for managing items inside a wishlist")
@RateLimited(action = RateLimitPlan.DEFAULT)
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/wishlists")
public class WishListItemController {

    private final WishListItemService wishListItemService;

    @Operation(summary = "Add an item", description = "Adds a new item to the specified wishlist.")
    @ApiUnauthorizedErrorDoc
    @ApiNotFoundErrorDoc
    @ApiForbiddenErrorDoc
    @RateLimited(action = RateLimitPlan.MUTATION)
    @PostMapping("/{wishlistId}")
    public ResponseEntity<WishListItemDto> createWishListItem(@PathVariable String wishlistId,
                                                              @Valid @RequestBody CreateWishListItemRequest createWishListItemRequest,
                                                              @AuthenticationPrincipal User user) {
        WishListItemDto savedItem = wishListItemService.createWishListItem(wishlistId, createWishListItemRequest, user);
        return ResponseEntity.created(URI.create("/api/v1/wishlists/" + wishlistId + "/wishes/" + savedItem.id())).body(savedItem);
    }

    @Operation(summary = "Get all items", description = "Retrieves all items for a specific wishlist.")
    @ApiNotFoundErrorDoc
    @GetMapping("/{wishlistId}/wishes")
    public ResponseEntity<WishListItemContainerDto> getAllWishlistItemsForWishlist(@PathVariable String wishlistId,
                                                                                   @AuthenticationPrincipal User user) {
        WishListItemContainerDto wishListItemContainerDto = wishListItemService.findAllWishListItemsForWishList(wishlistId, user);
        return ResponseEntity.ok(wishListItemContainerDto);
    }

    @Operation(summary = "Get a specific item", description = "Retrieves details of a single item within a wishlist.")
    @ApiNotFoundErrorDoc
    @GetMapping("/{wishlistId}/wishes/{wishlistItemId}")
    public ResponseEntity<WishListItemDto> getWishListItem(@PathVariable String wishlistId,
                                                           @PathVariable String wishlistItemId,
                                                           @AuthenticationPrincipal User user) {
        WishListItemDto wishlistItemDto = wishListItemService.findWishlistItemByIdAndWishlistId(wishlistId, wishlistItemId, user);
        return ResponseEntity.ok(wishlistItemDto);
    }

    @Operation(summary = "Delete an item", description = "Removes an item from the wishlist.")
    @ApiUnauthorizedErrorDoc
    @ApiNotFoundErrorDoc
    @ApiForbiddenErrorDoc
    @RateLimited(action = RateLimitPlan.MUTATION)
    @DeleteMapping("/{wishlistId}/wishes/{itemId}")
    public ResponseEntity<ApiResponseWrapper> deleteWishListItem(@PathVariable String wishlistId,
                                                                 @PathVariable String itemId,
                                                                 @AuthenticationPrincipal User user) {
        wishListItemService.deleteWishlistItem(wishlistId, itemId, user);
        return ResponseEntity.ok(new ApiResponseWrapper(
                String.format("Wishlist item with id: %s was successfully removed from the wishlist: %s", itemId, wishlistId)));
    }

    @Operation(summary = "Update an item", description = "Updates details of an existing wishlist item.")
    @ApiUnauthorizedErrorDoc
    @ApiNotFoundErrorDoc
    @ApiForbiddenErrorDoc
    @RateLimited(action = RateLimitPlan.MUTATION)
    @PatchMapping("/{wishlistId}/wishes/{itemId}")
    public ResponseEntity<WishListItemDto> updateWishlistItem(@PathVariable String wishlistId,
                                                              @PathVariable String itemId,
                                                              @Valid @RequestBody UpdateWishListItemRequest updateWishListItemRequest,
                                                              @AuthenticationPrincipal User user) {
        return ResponseEntity.ok().body(wishListItemService.updateWishlistItem(updateWishListItemRequest, wishlistId, itemId, user));
    }

    @Operation(summary = "Toggle item check status", description = "Marks a wishlist item as checked or unchecked.")
    @ApiNotFoundErrorDoc
    @RateLimited(action = RateLimitPlan.MUTATION)
    @PatchMapping("/{wishlistId}/wishes/{itemId}/checked")
    public ResponseEntity<ApiResponseWrapper> toggleItemCheck(@PathVariable String wishlistId,
                                                              @PathVariable String itemId,
                                                              @Valid @RequestBody WishlistItemIsCheckedRequest request,
                                                              @AuthenticationPrincipal User user) {
        wishListItemService.toggleIsChecked(wishlistId, itemId, request, user);
        return ResponseEntity.ok(new ApiResponseWrapper("Item status updated successfully"));
    }

}
