package com.api.wishoria.dto.payload.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for AI-based wishlist generation from a text description")
public record GenerateWishlistRequest(

        @Schema(
                description = "Text description of the wishlist to generate. Be specific about the occasion, theme, preferences, or budget.",
                example = "Birthday wishlist for a tech enthusiast who loves gaming and smart home devices, budget around $500",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Description must not be blank")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @Schema(description = "Whether the generated wishlist should be public", example = "false", defaultValue = "false")
        Boolean isPublic
) {}
