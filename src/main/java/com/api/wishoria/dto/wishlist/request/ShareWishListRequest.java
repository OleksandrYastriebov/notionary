package com.api.wishoria.dto.wishlist.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for sharing a private wishlist with another user")
public record ShareWishListRequest(
        @Schema(description = "Email of the user to share the wishlist with", example = "friend@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {
}
