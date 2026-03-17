package com.api.wishoria.dto.token;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing a new access token after refresh")
public record TokenRefreshDto(
        @Schema(description = "New JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI...")
        String accessToken,

        @Schema(description = "Token type", example = "Bearer", defaultValue = "Bearer")
        String tokenType
) {
    public TokenRefreshDto(String accessToken) {
        this(accessToken, "Bearer");
    }
}
