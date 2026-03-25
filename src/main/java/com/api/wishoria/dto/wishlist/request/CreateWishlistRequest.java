package com.api.wishoria.dto.wishlist.request;

import com.api.wishoria.entity.User;
import com.api.wishoria.entity.WishList;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

@Schema(description = "Payload for creating a new wishlist")
public record CreateWishlistRequest(
        @Schema(description = "Name of the wishlist", example = "My Birthday Wishlist", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Title can not be empty")
        @Size(max = 100, message = "Title is too long. Max 100 characters.")
        String title,

        @Schema(description = "Visibility of the wishlist", example = "true", defaultValue = "false")
        Boolean isPublic,

        @Schema(description = "Cover image URL for the wishlist", example = "https://example.com/cover.jpg")
        @Size(max = 2048, message = "Image URL is too long")
        @URL(message = "Invalid URL format")
        String imageUrl
) {
    public WishList toEntity(User user) {
        return new WishList(
                user,
                title,
                isPublic != null ? isPublic : false,
                imageUrl);
    }
}
