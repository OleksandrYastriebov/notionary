package com.api.notionary.dto.payload.request.token;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for refreshing an access token")
public record TokenRefreshRequest(
        @Schema(description = "Valid refresh token string",
                example = "550e8400-e29b-41d4-a716-446655440000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Refresh token is required for logout")
        @Size(min = 1, max = 50, message = "The input is too long. Max 50 characters.")
        String refreshToken
) {
}
