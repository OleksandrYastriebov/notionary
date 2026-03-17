package com.api.wishoria.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ai assistant generated description")
public record AiDescriptionDto(
        @Schema(description = "Generated description text", example = "String")
        String description
) {
}