package com.api.notionary.dto.payload.request.wishlistitem;

import com.api.notionary.entity.WishList;
import com.api.notionary.entity.WishListItem;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.validator.constraints.URL;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Payload for adding a new item to a wishlist")
public record CreateWishListItemRequest(
        @Schema(description = "Item name", example = "Sony PlayStation 5", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Title cannot be empty")
        @Size(max = 100, message = "Title must not exceed 100 characters")
        String title,

        @Schema(description = "Link to the product store", example = "https://store.sony.com/ps5")
        @Size(max = 2048, message = "URL is too long")
        @URL(message = "Invalid URL format")
        String url,

        @Schema(description = "Estimated price", example = "499.99")
        @DecimalMin(value = "0.0", message = "Price must be positive")
        @Digits(integer = 8, fraction = 2, message = "Price format is invalid (e.g. 12345678.99)")
        BigDecimal price,

        @Schema(description = "Extra details or notes", example = "Disc edition preferably")
        @Size(max = 1000, message = "Description should not more than 1000 characters")
        String description,

        @Schema(description = "Product image URL", example = "https://example.com/ps5.jpg")
        @Size(max = 2048, message = "Image URL is too long")
        @URL(message = "Invalid URL format")
        String imageUrl
) {
    public WishListItem toEntity(WishList wishList) {
        return new WishListItem(
                wishList,
                title,
                url,
                description,
                price,
                imageUrl);
    }

}
