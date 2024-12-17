package com.api.notionary.controller;

import com.api.notionary.dto.WishlistItemDto;
import com.api.notionary.dto.WishlistItemIsCheckedRequestDto;
import com.api.notionary.service.WishListItemService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

import static com.api.notionary.util.constants.Constant.REQUEST_BODY_IS_MISSING_OR_INVALID_MESSAGE;

@RestController
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
                                                @RequestBody(required = false) WishlistItemDto wishlistItemDto, Authentication authentication) {
        if (wishlistItemDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(REQUEST_BODY_IS_MISSING_OR_INVALID_MESSAGE);
        }
        String userEmail = CredentialUtils.getAuthenticatedUserEmail(authentication);

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
        String userEmail = CredentialUtils.getAuthenticatedUserEmail(authentication);

        if (!wishlistService.isWishlistOwner(wishlistId, userEmail)) {
            return ResponseEntity.status(403).body("You need to be owner to remove a wishlist item.");
        }
        wishListItemService.deleteWishlistItem(wishlistId, itemId);
        return ResponseEntity.ok().body(
                String.format("Wishlist item with id: %s was successfully removed from the wishlist: %s", itemId, wishlistId));
    }

    @PutMapping("/{wishlistId}/wish/{itemId}")
    public ResponseEntity<?> updateWishlistItem(@PathVariable String wishlistId, @PathVariable String itemId,
                                                @RequestBody(required = false) WishlistItemDto wishlistItemDto, Authentication authentication) {
        if (wishlistItemDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(REQUEST_BODY_IS_MISSING_OR_INVALID_MESSAGE);
        }

        String userEmail = CredentialUtils.getAuthenticatedUserEmail(authentication);

        if (!wishlistService.isWishlistOwner(wishlistId, userEmail)) {
            return ResponseEntity.status(403).body("You need to be owner to update a wishlist item.");
        }
        return ResponseEntity.ok().body(wishListItemService.updateWishlistItem(wishlistItemDto, wishlistId, itemId));
    }

    @PatchMapping("/{wishlistId}/wish/{itemId}/checked")
    public ResponseEntity<?> updateIsChecked(@Valid @RequestBody(required = false) WishlistItemIsCheckedRequestDto isCheckedDto,
                                             @PathVariable String wishlistId,
                                             @PathVariable String itemId,
                                             Authentication authentication) {
        if (isCheckedDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(REQUEST_BODY_IS_MISSING_OR_INVALID_MESSAGE);
        }
        String userEmail = CredentialUtils.getAuthenticatedUserEmail(authentication);

        if (!wishlistService.isWishlistOwner(wishlistId, userEmail) && !wishlistService.isPublic(wishlistId)) {
            return ResponseEntity.status(403).body("This is private wishlist. You don't have permissions to change it.");
        }
        wishListItemService.updateIsChecked(isCheckedDto, wishlistId, itemId);
        return ResponseEntity.ok().body(String.format(
                "Updated isChecked property to %s in wishlist item: %s in wishlist: %s", isCheckedDto.getIsChecked(), wishlistId, itemId));
    }
}
