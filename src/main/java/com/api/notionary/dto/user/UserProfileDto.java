package com.api.notionary.dto.user;

import com.api.notionary.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "User profile information")
public record UserProfileDto(
        @Schema(description = "Unique user identifier", example = "10")
        Long id,

        @Schema(description = "User's first name", example = "John")
        String firstName,

        @Schema(description = "User's last name", example = "Doe")
        String lastName,

        @Schema(description = "User's email address", example = "john.doe@notionary.app")
        String email,

        @Schema(description = "URL to the user's avatar image", example = "https://example.com/avatars/user.jpg")
        String avatarUrl,

        @Schema(description = "Timestamp when the account was created", example = "2023-10-01T12:00:00Z")
        Instant createdAt
) {
    public UserProfileDto(User user) {
        this(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getCreatedAt()
        );
    }
}
