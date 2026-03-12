package com.api.notionary.dto.payload.request.wishlist;

import com.api.notionary.entity.WishList;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

@Schema(description = "Payload for updating an existing wishlist")
public record UpdateWishlistRequest(
        @Schema(description = "New title", example = "Updated Christmas List")
        @Size(min = 1, max = 100, message = "Title is too long. Max 100 characters.")
        String title,

        @Schema(description = "Change visibility", example = "false")
        Boolean isPublic,

        @Schema(description = "New cover image URL", example = "https://example.com/new-cover.jpg")
        @Size(max = 2048, message = "Image URL is too long")
        @URL(message = "Invalid URL format")
        String imageUrl
) {
    public void updateEntity(WishList existingWishlist) {
        if (this.title != null) existingWishlist.setTitle(this.title);
        if (this.isPublic != null) existingWishlist.setIsPublic(this.isPublic);
        if (this.imageUrl != null) existingWishlist.setImageUrl(this.imageUrl);
    }

}
