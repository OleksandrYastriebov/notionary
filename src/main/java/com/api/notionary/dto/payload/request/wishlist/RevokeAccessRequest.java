package com.api.notionary.dto.payload.request.wishlist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for revoking access to a private wishlist")
public record RevokeAccessRequest(
        @Schema(description = "Email of the user to remove", example = "friend@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {
}