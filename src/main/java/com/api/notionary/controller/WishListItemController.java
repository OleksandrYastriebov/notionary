package com.api.notionary.controller;

import com.api.notionary.dto.WishlistItemDto;
import com.api.notionary.service.WishListItemService;
import com.api.notionary.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;

@Controller
@RequestMapping("/api/wishlist")
public class WishListItemController {

    private final WishListItemService wishListItemService;
    private final WishlistService wishlistService;

    @Autowired
    public WishListItemController(WishListItemService wishListItemService, WishlistService wishlistService) {
        this.wishListItemService = wishListItemService;
        this.wishlistService = wishlistService;
    }

    @PostMapping("/{wishlistId}")
    public ResponseEntity<?> createWishListItem(@PathVariable String wishlistId,
                                                @RequestBody WishlistItemDto wishlistItemDto, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("You need to be authorised to create a wishlist item.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userEmail = userDetails.getUsername();

        if (!wishlistService.isWishlistOwner(wishlistId, userEmail)) {
            return ResponseEntity.status(403).body("You need to be owner to add a wishlist items.");
        }

        WishlistItemDto savedItem = wishListItemService.addWishListItem(wishlistId, wishlistItemDto);

        return ResponseEntity.created(URI.create("/api/wishlist/" + wishlistId + "/wish/" + savedItem.getId())).body(savedItem);
    }

    @GetMapping("/{wishlistId}/wish/{wishlistItemId}")
    public ResponseEntity<WishlistItemDto> getWishListItem(@PathVariable String wishlistId,
                                                           @PathVariable String wishlistItemId) {

        WishlistItemDto wishlistItemDto = wishListItemService.findWishlistItemByIdAndWishlistId(wishlistItemId, wishlistId);
        return ResponseEntity.ok().body(wishlistItemDto);
    }

    @DeleteMapping("/{wishlistId}/wish/{itemId}")
    public ResponseEntity<String> deleteWishListItem(@PathVariable String wishlistId, @PathVariable String itemId,
                                                Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("You need to be authorised to delete a wishlist item.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userEmail = userDetails.getUsername();

        if (!wishlistService.isWishlistOwner(wishlistId, userEmail)) {
            return ResponseEntity.status(403).body("You need to be owner to remove a wishlist item.");
        }
        wishListItemService.deleteWishlistItem(wishlistId, itemId);
        return ResponseEntity.ok().body(
                String.format("Wishlist item with id: %s was successfully removed from the wishlist: %s", itemId, wishlistId));
    }

    @PutMapping("/{wishlistId}/wish/{itemId}")
    public ResponseEntity<?> updateWishlistItem(@PathVariable String wishlistId, @PathVariable String itemId,
                                                @RequestBody WishlistItemDto wishlistItemDto, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("You need to be authorised to update a wishlist item.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userEmail = userDetails.getUsername();

        if (!wishlistService.isWishlistOwner(wishlistId, userEmail)) {
            return ResponseEntity.status(403).body("You need to be owner to update a wishlist item.");
        }
        return ResponseEntity.ok().body("SDf");
    }
}
