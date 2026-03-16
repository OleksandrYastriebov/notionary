package com.api.notionary.dto.payload.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "AI-based generation request")
public record GenerateDescriptionRequest(

        @Schema(
                description = "Wish title (required)",
                example = "Sneakers Nike Air Max",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 2,
                maxLength = 150
        )
        @Size(max = 100, message = "Wish title must be 100 characters max")
        String title,

        @Schema(
                description = "Base64 String of image (optional, sending from frontend)",
                example = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD...",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String base64Image,

        @Schema(
                description = "MIME image type (required if image is sent)",
                example = "image/jpeg",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                maxLength = 50
        )
        @Size(max = 50, message = "MIME type must not exceed 50 characters")
        String mimeType
) {
}