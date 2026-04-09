package com.api.wishoria.dto.user;

import com.api.wishoria.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "User profile information")
public record UserProfileDto(
        @Schema(description = "Unique user identifier", example = "10")
        Long id,

        @Schema(description = "User's first name", example = "John")
        String firstName,

        @Schema(description = "User's last name", example = "Doe")
        String lastName,

        @Schema(description = "User's email address", example = "john.doe@wishoria.app")
        String email,

        @Schema(description = "URL to the user's avatar image", example = "https://example.com/avatars/user.jpg")
        String avatarUrl,

        @Schema(description = "Timestamp when the account was created", example = "2023-10-01T12:00:00Z")
        Instant createdAt,

        @Schema(description = "User's profile description", example = "I love gadgets and outdoor sports.")
        String profileDescription,

        @Schema(description = "Whether the user profile is private", example = "false")
        boolean isPrivate,

        @Schema(description = "User's date of birth", example = "1995-06-15")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate dateOfBirth,

        @Schema(description = "Whether the user has opted in to marketing emails", example = "true")
        boolean emailMarketingConsent
) {
    public UserProfileDto(User user) {
        this(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getCreatedAt(),
                user.getProfileDescription(),
                user.isPrivateProfile(),
                user.getDateOfBirth(),
                user.isEmailMarketingConsent()
        );
    }
}
