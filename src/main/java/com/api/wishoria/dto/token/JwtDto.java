package com.api.wishoria.dto.token;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT Token response containing access and refresh tokens")
public record JwtDto(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI...")
        String jwtToken,

        @Schema(description = "Token type", example = "Bearer", defaultValue = "Bearer")
        String type,

        @Schema(description = "Unique user identifier", example = "10")
        Long id,

        @Schema(description = "User email address", example = "john.doe@wishoria.app")
        String email
) {
    public JwtDto(String jwtToken, Long id, String email) {
        this(jwtToken, "Bearer", id, email);
    }
}
