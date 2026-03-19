package com.api.wishoria.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Brief user information used for quick search (autocomplete) during sharing")
public record UserAutocompleteDto(

        @Schema(description = "Unique identifier of the user", example = "105")
        Long id,

        @Schema(description = "User's first name", example = "Alex")
        String firstName,

        @Schema(description = "User's last name", example = "Smith")
        String lastName,

        @Schema(description = "User's email address", example = "alex.smith@example.com")
        String email,

        @Schema(description = "URL to the user's profile picture. Can be null if the user has no photo.",
                example = "https://res.cloudinary.com/image.jpg",
                nullable = true)
        String avatarUrl
) {
}