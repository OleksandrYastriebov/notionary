package com.api.notionary.dto.payload.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for logging out a user")
public record LogOutRequest(
        @Schema(description = "Refresh token to be invalidated",
                example = "550e8400-e29b-41d4-a716-446655440000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Refresh token is required for logout")
        @Size(min = 1, max = 50, message = "The input is too long")
        String refreshToken
) {
}