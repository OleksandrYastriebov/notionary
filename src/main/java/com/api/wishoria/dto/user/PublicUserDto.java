package com.api.wishoria.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Public Information about the User for search and profile информация о пользователе для поиска и профиля")
public record PublicUserDto(
        @Schema(description = "Unique identifier of the user",
                example = "43",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(description = "User's first name",
                example = "Alex",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String firstName,

        @Schema(description = "User's last name",
                example = "Smith",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String lastName,

        @Schema(description = "Secure URL to the user's avatar image. Can be null if the user hasn't uploaded an avatar",
                example = "https://res.cloudinary.com/demo/image/upload/v1234567890/avatar.jpg",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String avatarUrl,

        @Schema(description = "User's profile description. Can be null if the user hasn't set one",
                example = "I love gadgets and outdoor sports.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String profileDescription
) {
}
