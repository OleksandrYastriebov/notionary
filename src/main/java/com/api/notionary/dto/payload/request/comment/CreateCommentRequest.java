package com.api.notionary.dto.payload.request.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Schema(description = "Request payload for comment text")
public record CreateCommentRequest(
        @Schema(description = "Valid comment text",
                example = "Hello, World!",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Comment text cannot be empty")
        @Size(max = 1000, message = "Comment is too long")
        String text
) {
}