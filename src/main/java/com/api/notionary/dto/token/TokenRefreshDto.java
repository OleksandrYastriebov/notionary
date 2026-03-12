package com.api.notionary.dto.token;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Schema(description = "Response containing a new access token after refresh")
public record TokenRefreshDto(
        @Schema(description = "New JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI...")
        String accessToken,

        @Schema(description = "New refresh token", example = "123e4567-e89b-12d3-a456-426614174000")
        String refreshToken,

        @Schema(description = "Token type", example = "Bearer", defaultValue = "Bearer")
        String tokenType
) {
    public TokenRefreshDto(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, "Bearer");
    }
}
