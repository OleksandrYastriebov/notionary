package com.api.wishoria.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Data Transfer Object representing an uploaded image")
public record ImageDto(@Schema(
        description = "Secure URL of the uploaded image",
        example = "https://res.cloudinary.com/demo/image/upload/v1234567/sample.jpg") String url) {
}
