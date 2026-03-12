package com.api.notionary.controller;

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

@RateLimited(action = RateLimitPlan.DEFAULT)
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/wishlists")
public class WishListItemController {

    private final WishListItemService wishListItemService;

    @RateLimited(action = RateLimitPlan.MUTATION)
    @PostMapping("/{wishlistId}")
    public ResponseEntity<WishListItemDto> createWishListItem(@PathVariable String wishlistId,
                                                              @Valid @RequestBody CreateWishListItemRequest createWishListItemRequest,
                                                              @AuthenticationPrincipal User user) {
        WishListItemDto savedItem = wishListItemService.createWishListItem(wishlistId, createWishListItemRequest, user);
        return ResponseEntity.created(URI.create("/api/v1/wishlists/" + wishlistId + "/wishes/" + savedItem.getId())).body(savedItem);
    }

    @GetMapping("/{wishlistId}/wishes")
    public ResponseEntity<WishListItemContainerDto> getAllWishlistItemsForWishlist(@PathVariable String wishlistId,
                                                                                   @AuthenticationPrincipal User user) {
        WishListItemContainerDto wishListItemContainerDto = wishListItemService.findAllWishListItemsForWishList(wishlistId, user);
        return ResponseEntity.ok(wishListItemContainerDto);
    }

    @GetMapping("/{wishlistId}/wishes/{wishlistItemId}")
    public ResponseEntity<WishListItemDto> getWishListItem(@PathVariable String wishlistId,
                                                           @PathVariable String wishlistItemId,
                                                           @AuthenticationPrincipal User user) {
        WishListItemDto wishlistItemDto = wishListItemService.findWishlistItemByIdAndWishlistId(wishlistId, wishlistItemId, user);
        return ResponseEntity.ok(wishlistItemDto);
    }

    @RateLimited(action = RateLimitPlan.MUTATION)
    @DeleteMapping("/{wishlistId}/wishes/{itemId}")
    public ResponseEntity<ApiResponseWrapper> deleteWishListItem(@PathVariable String wishlistId,
                                                                 @PathVariable String itemId,
                                                                 @AuthenticationPrincipal User user) {
        wishListItemService.deleteWishlistItem(wishlistId, itemId, user);
        return ResponseEntity.ok(new ApiResponseWrapper(
                String.format("Wishlist item with id: %s was successfully removed from the wishlist: %s", itemId, wishlistId)));
    }

    @RateLimited(action = RateLimitPlan.MUTATION)
    @PatchMapping("/{wishlistId}/wishes/{itemId}")
    public ResponseEntity<WishListItemDto> updateWishlistItem(@PathVariable String wishlistId,
                                                              @PathVariable String itemId,
                                                              @Valid @RequestBody UpdateWishListItemRequest updateWishListItemRequest,
                                                              @AuthenticationPrincipal User user) {
        return ResponseEntity.ok().body(wishListItemService.updateWishlistItem(updateWishListItemRequest, wishlistId, itemId, user));
    }

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
