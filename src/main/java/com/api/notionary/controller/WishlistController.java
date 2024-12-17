package com.api.notionary.controller;

import com.api.notionary.dto.WishlistDto;
import com.api.notionary.dto.WishlistTitleRequestDto;
import com.api.notionary.dto.WishlistVisibilityRequestDto;
import com.api.notionary.entity.User;
import com.api.notionary.service.WishlistService;
import com.api.notionary.util.CredentialUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

import static com.api.notionary.util.constants.Constant.REQUEST_BODY_IS_MISSING_OR_INVALID_MESSAGE;

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

        return ResponseEntity.ok().body(wishlistService.getWishlistsForUser(user));
    }

    @GetMapping("/{wishlistId}")
    public ResponseEntity<?> getWishlist(@PathVariable String wishlistId, Authentication authentication) {

        String userEmail = CredentialUtils.getAuthenticatedUserEmail(authentication);

        if (!wishlistService.isWishlistOwner(wishlistId, userEmail) && !wishlistService.isPublic(wishlistId)) {
            return ResponseEntity.status(403).body("This is private wishlist. You don't have permissions to see it.");
        }

        return ResponseEntity.ok().body(wishlistService.findWishlistWithItemsById(wishlistId));
    }

    @PostMapping
    public ResponseEntity<?> createWishList(@RequestBody(required = false) WishlistDto wishlistDto,
                                            Authentication authentication) {
        if (wishlistDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(REQUEST_BODY_IS_MISSING_OR_INVALID_MESSAGE);
        }

        String userEmail = CredentialUtils.getAuthenticatedUserEmail(authentication);

        WishlistDto wishlist = wishlistService.saveWishlist(wishlistDto, userEmail);
        return ResponseEntity.created(URI.create("/api/wishlist/" + wishlist.getId())).body(wishlist);
    }

    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<?> deleteWishList(@PathVariable String wishlistId, Authentication authentication) {
        String userEmail = CredentialUtils.getAuthenticatedUserEmail(authentication);

        if (!wishlistService.isWishlistOwner(wishlistId, userEmail)) {
            return ResponseEntity.status(403).body("You need to be owner to remove a wishlist.");
        }

        wishlistService.deleteWishList(wishlistId);
        return ResponseEntity.ok().body(String.format("Wishlist with id %s was successfully removed from database", wishlistId));
    }

    @PatchMapping("/{wishlistId}/visibility")
    public ResponseEntity<?> changeVisibility(@PathVariable String wishlistId,
                                              @Valid @RequestBody(required = false) WishlistVisibilityRequestDto isPublicDto,
                                              Authentication authentication) {
        if (isPublicDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(REQUEST_BODY_IS_MISSING_OR_INVALID_MESSAGE);
        }

        String userEmail = CredentialUtils.getAuthenticatedUserEmail(authentication);

        if (!wishlistService.isWishlistOwner(wishlistId, userEmail)) {
            return ResponseEntity.status(403).body("You need to be owner to change a wishlist visibility.");
        }
        wishlistService.updateVisibility(wishlistId, isPublicDto);

        return ResponseEntity.ok().body(String.format("Visibility of wishlist with id: %s was changed to %s", wishlistId, isPublicDto.getIsPublic()));
    }

    @PatchMapping("/{wishlistId}/title")
    public ResponseEntity<?> changeTitle(@PathVariable String wishlistId,
                                         @Valid @RequestBody(required = false) WishlistTitleRequestDto titleDto,
                                         Authentication authentication) {
        if (titleDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(REQUEST_BODY_IS_MISSING_OR_INVALID_MESSAGE);
        }

        String userEmail = CredentialUtils.getAuthenticatedUserEmail(authentication);

        if (!wishlistService.isWishlistOwner(wishlistId, userEmail)) {
            return ResponseEntity.status(403).body("You need to be owner to change a wishlist title.");
        }
        wishlistService.updateTitle(wishlistId, titleDto);

        return ResponseEntity.ok().body(String.format("Title of wishlist with id: %s was changed to \"%s\"", wishlistId, titleDto.getTitle()));
    }

}
