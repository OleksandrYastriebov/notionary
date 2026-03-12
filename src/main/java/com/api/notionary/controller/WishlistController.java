package com.api.notionary.controller;

import com.api.notionary.dto.payload.request.wishlist.CreateWishlistRequest;
import com.api.notionary.dto.payload.request.wishlist.UpdateWishlistRequest;
import com.api.notionary.dto.wishlist.WishListContainerDto;
import com.api.notionary.dto.wishlist.WishListDto;
import com.api.notionary.dto.ApiResponseWrapper;
import com.api.notionary.entity.User;
import com.api.notionary.security.interceptor.RateLimitPlan;
import com.api.notionary.security.interceptor.RateLimited;
import com.api.notionary.service.WishListService;
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
public class WishlistController {

    private final WishListService wishlistService;

    @GetMapping
    public ResponseEntity<WishListContainerDto> getWishlists(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok().body(wishlistService.getWishlistsForUser(user));
    }

    @GetMapping("/{wishlistId}")
    public ResponseEntity<WishListDto> getWishlist(@PathVariable String wishlistId, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(wishlistService.findWishlistById(wishlistId, user));
    }

    @RateLimited(action = RateLimitPlan.MUTATION)
    @PostMapping
    public ResponseEntity<?> createWishList(@RequestBody CreateWishlistRequest createWishlistRequest,
                                            @AuthenticationPrincipal User user) {
        WishListDto wishlist = wishlistService.createWishlist(createWishlistRequest, user);
        return ResponseEntity.created(URI.create("/api/v1/wishlists/" + wishlist.getId())).body(wishlist);
    }

    @RateLimited(action = RateLimitPlan.MUTATION)
    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<ApiResponseWrapper> deleteWishList(@PathVariable String wishlistId, @AuthenticationPrincipal User user) {
        wishlistService.deleteWishList(wishlistId, user);
        return ResponseEntity.ok(new ApiResponseWrapper(String.format("Wishlist with id %s was successfully removed from database", wishlistId)));
    }

    @RateLimited(action = RateLimitPlan.MUTATION)
    @PatchMapping("/{wishlistId}")
    public ResponseEntity<ApiResponseWrapper> updateWishlist(@PathVariable String wishlistId,
                                                             @Valid @RequestBody UpdateWishlistRequest updateWishlistRequest,
                                                             @AuthenticationPrincipal User user) {

        WishListDto wishListDto = wishlistService.updateWishlist(wishlistId, updateWishlistRequest, user);
        return ResponseEntity.ok(new ApiResponseWrapper(String.format("Updated Wishlist: %s", wishListDto)));
    }
}
