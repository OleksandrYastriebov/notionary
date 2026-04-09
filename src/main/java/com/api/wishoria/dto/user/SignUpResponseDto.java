package com.api.wishoria.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned after successful user registration")
public record SignUpResponseDto(
        @Schema(example = "User registered successfully. Please check your email to activate your account.")
        String message,
        @Schema(example = "42", description = "ID of the newly created user — used for analytics profile ingestion")
        Long userId
) {
}
