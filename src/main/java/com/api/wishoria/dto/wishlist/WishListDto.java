package com.api.wishoria.dto.wishlist;

import com.api.wishoria.dto.wishlistitem.WishListItemDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Details of a wishlist including its items")
public record WishListDto(
        @Schema(description = "Unique wishlist identifier", example = "uuid-1234")
        String id,

        @Schema(description = "Owner's user ID", example = "10")
        Long userId,

        @Schema(description = "List of items in this wishlist")
        List<WishListItemDto> wishListItems,

        @Schema(description = "Title of the wishlist", example = "My Birthday Wishlist")
        String title,

        @Schema(description = "Visibility status", example = "false")
        Boolean isPublic,

        @Schema(description = "Cover image URL", example = "https://example.com/cover.jpg")
        String imageUrl,

        @Schema(description = "Creation timestamp")
        Instant createdAt
) {
}