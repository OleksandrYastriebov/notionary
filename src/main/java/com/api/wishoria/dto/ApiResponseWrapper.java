package com.api.wishoria.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard API response wrapper")
public record ApiResponseWrapper(
        @Schema(example = "User with id 10 was successfully removed.", description = "Human-readable message")
        String message
) {
}
