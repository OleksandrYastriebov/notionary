package com.api.wishoria.dto.wishlistitem.request;

import com.api.wishoria.entity.WishListItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

@Schema(description = "Payload for modifying an existing wishlist item")
public record UpdateWishListItemRequest(
        @Schema(description = "Updated item name", example = "Sony PlayStation 5 Slim")
        @Size(min = 1, max = 100, message = "Title should be between 1 and 100 characters")
        String title,

        @Schema(description = "Updated product URL", example = "https://store.sony.com/ps5-slim")
        @Size(max = 2048, message = "URL is too long")
        @URL(message = "Invalid URL format")
        String url,

        @Schema(description = "Updated price", example = "449.99")
        @DecimalMin(value = "0.0", message = "Price must be positive")
        @Digits(integer = 8, fraction = 2, message = "Price format is invalid (e.g. 12345678.99)")
        BigDecimal price,

        @Schema(description = "Updated description", example = "Changed my mind, slim edition is better")
        @Size(max = 1000, message = "Description should not more than 1000 characters")
        String description,

        @Schema(description = "Whether the item is fulfilled/purchased", example = "false")
        Boolean isChecked,

        @Schema(description = "Updated image URL", example = "https://example.com/ps5-slim.jpg")
        @Size(max = 2048, message = "Image URL is too long")
        @URL(message = "Invalid URL format")
        String imageUrl
) {
    public void updateEntity(WishListItem existingItem) {
        if (this.title != null) existingItem.setTitle(this.title);
        if (this.url != null) existingItem.setUrl(this.url);
        if (this.price != null) existingItem.setPrice(this.price);
        if (this.description != null) existingItem.setDescription(this.description);
        if (this.isChecked != null) existingItem.setChecked(this.isChecked);
        if (this.imageUrl != null) existingItem.setImageUrl(this.imageUrl);
    }
}
