package com.api.notionary.dto.wishlistitem;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Details of a single wishlist item")
public record WishListItemDto(
        @Schema(description = "Unique item identifier", example = "item-uuid-5678")
        String id,

        @Schema(description = "ID of the parent wishlist", example = "uuid-1234")
        String wishListId,

        @Schema(description = "Item title", example = "Sony PlayStation 5")
        String title,

        @Schema(description = "Product URL", example = "https://store.sony.com/ps5")
        String url,

        @Schema(description = "Item price", example = "499.99")
        BigDecimal price,

        @Schema(description = "Item description or notes", example = "Disc edition preferably")
        String description,

        @Schema(description = "Item image URL", example = "https://example.com/ps5.jpg")
        String imageUrl,

        @Schema(description = "Whether the item is fulfilled or purchased", example = "false")
        Boolean isChecked,

        @Schema(description = "ID of the user who reserved the item, null if not reserved")
        Long checkedByUserId,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt
) {
}