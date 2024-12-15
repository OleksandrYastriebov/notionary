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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

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
            return ResponseEntity.ok(List.of());
        }
        User user = (User) authentication.getPrincipal();
        wishlistService.getWishlistsForUser(user);

        return ResponseEntity.ok().body(wishlistService.getWishlistsForUser(user));
    }

    @PostMapping
    public ResponseEntity<?> createWishList(@RequestBody WishlistDto wishlistDto, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("You need to be authorised to create a wishlist.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userEmail = userDetails.getUsername();
        WishlistDto wishlist = wishlistService.saveWishlist(wishlistDto, userEmail);
        return ResponseEntity.created(URI.create("/wishlists/" + wishlist.getId())).body(wishlist);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWishList(@PathVariable String id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("You need to be authorised to remove a wishlist.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userEmail = userDetails.getUsername();

        if(!wishlistService.isWishlistOwner(id, userEmail)) {
            return ResponseEntity.status(403).body("You need to be owner to remove a wishlist.");
        }

        wishlistService.deleteWishList(id);
        return ResponseEntity.ok().body(String.format("Wishlist with id %s was successfully removed from database", id));
    }

}
