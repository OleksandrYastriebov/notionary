package com.api.notionary.controller;

import com.api.notionary.dto.WishlistDto;
import com.api.notionary.entity.User;
import com.api.notionary.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    @Autowired
    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<?> getWishlists(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("You need to be logged in to view your wishlists.");
        }
        User user = (User) authentication.getPrincipal();
        wishlistService.getWishlistsForUser(user);

        return ResponseEntity.ok().body(wishlistService.getWishlistsForUser(user));
    }

    @GetMapping("/{wishlistId}")
    public ResponseEntity<?> getWishlist(@PathVariable String wishlistId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("You need to be logged in to view your wishlist.");
        }

        UserDetails user = (UserDetails) authentication.getPrincipal();
        String userEmail = user.getUsername();

        if (!wishlistService.isWishlistOwner(wishlistId, userEmail) && !wishlistService.isPublic(wishlistId)) {
            return ResponseEntity.status(403).body("This is private wishlist. You don't have permissions to see it.");
        }

        return ResponseEntity.ok().body(wishlistService.findWishlistWithItemsById(wishlistId));
    }

    @PostMapping
    public ResponseEntity<?> createWishList(@RequestBody WishlistDto wishlistDto, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("You need to be authorised to create a wishlist.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userEmail = userDetails.getUsername();

        WishlistDto wishlist = wishlistService.saveWishlist(wishlistDto, userEmail);
        return ResponseEntity.created(URI.create("/api/wishlist/" + wishlist.getId())).body(wishlist);
    }

    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<?> deleteWishList(@PathVariable String wishlistId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("You need to be authorised to remove a wishlist.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userEmail = userDetails.getUsername();

        if (!wishlistService.isWishlistOwner(wishlistId, userEmail)) {
            return ResponseEntity.status(403).body("You need to be owner to remove a wishlist.");
        }

        wishlistService.deleteWishList(wishlistId);
        return ResponseEntity.ok().body(String.format("Wishlist with id %s was successfully removed from database", wishlistId));
    }

    @PatchMapping("/{wishlistId}/visibility")
    public ResponseEntity<?> changeVisibility(@PathVariable String wishlistId, @RequestBody Boolean isPublic,
                                              Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("You need to be authorised to hange a wishlist visibility.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userEmail = userDetails.getUsername();

        if (!wishlistService.isWishlistOwner(wishlistId, userEmail)) {
            return ResponseEntity.status(403).body("You need to be owner to change a wishlist visibility.");
        }
        wishlistService.changeVisibility(wishlistId, isPublic);

        return ResponseEntity.ok().body(String.format("Visibility of wishlist with id: %s was changed to %s", wishlistId, isPublic));
    }

    @PatchMapping("/{wishlistId}/title")
    public ResponseEntity<?> changeTitle(@PathVariable String wishlistId, @RequestBody String title,
                                         Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("You need to be authorised to change a wishlist title.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userEmail = userDetails.getUsername();

        if (!wishlistService.isWishlistOwner(wishlistId, userEmail)) {
            return ResponseEntity.status(403).body("You need to be owner to change a wishlist title.");
        }
        wishlistService.changeTitle(wishlistId, title);

        return ResponseEntity.ok().body(String.format("Title of wishlist with id: %s was changed to %s", wishlistId, title));
    }

}
