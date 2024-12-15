package com.api.notionary.controller;

import com.api.notionary.dto.WishlistDto;
import com.api.notionary.dto.WishlistItemDto;
import com.api.notionary.entity.WishListItem;
import com.api.notionary.service.WishListItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/wishlist")
public class WishListItemController {

    private WishListItemService wishListItemService;

    @Autowired
    public WishListItemController(WishListItemService wishListItemService) {
        this.wishListItemService = wishListItemService;
    }

    @PostMapping("/{wishlistId}")
    public ResponseEntity<?> createWishListItem(@PathVariable String wishlistId,
                                                @RequestBody WishlistItemDto wishlistItemDto, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("You need to be authorised to create a wishlist item.");
        }

        WishlistItemDto item = wishListItemService.addWishListItem(wishlistId, wishlistItemDto);

        return null;
    }

   /* @GetMapping("/{wishlistId}/wish")
    public ResponseEntity<List<WishlistItemDto>> getWishListItems(@PathVariable String wishlistId) {
        return ResponseEntity.ok().body(List.of());
    }

    @GetMapping("/{wishlistId}/wish/{itemId}")
    public ResponseEntity<WishlistItemDto> getWishListItem(@PathVariable String wishlistId, @PathVariable String itemId) {
        return ResponseEntity.ok().body();
    }*/

/*    @DeleteMapping("/{wishlistId}/wish/{itemId}")
    public ResponseEntity<?> deleteWishListItem(@PathVariable String wishlistId, @PathVariable String itemId) {
        return ResponseEntity.ok().body();
    }*/
}
