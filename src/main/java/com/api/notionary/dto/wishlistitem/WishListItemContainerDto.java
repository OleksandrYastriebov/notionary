package com.api.notionary.dto.wishlistitem;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Container holding a list of wishlist items")
public record WishListItemContainerDto(
        @Schema(description = "Array of wishlist items")
        List<WishListItemDto> wishListItems
) {
}