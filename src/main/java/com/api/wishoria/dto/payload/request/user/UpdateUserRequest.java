package com.api.wishoria.dto.payload.request.user;

import com.api.wishoria.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

@Schema(description = "Payload for updating user profile data. Omitted fields will not be updated.")
public record UpdateUserRequest(
        @Schema(description = "New first name", example = "Johnny")
        @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
        String firstName,

        @Schema(description = "New last name", example = "Smith")
        @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
        String lastName,

        @Schema(description = "URL to the new avatar image", example = "https://example.com/avatars/new.jpg")
        @Size(max = 2048, message = "Avatar URL is too long")
        @URL(message = "Invalid URL format")
        String avatarUrl,

        @Schema(description = "Profile description", example = "I love gadgets and outdoor sports.")
        @Size(max = 1000, message = "Profile description must not exceed 1000 characters")
        String profileDescription,

        @Schema(description = "Whether the profile should be private", example = "false")
        Boolean isPrivate,

        @Schema(description = "User's date of birth", example = "1995-06-15")
        @Past(message = "Date of birth must be in the past")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate dateOfBirth
) {
    public void updateEntity(User user) {
        if (this.firstName != null) user.setFirstName(this.firstName);
        if (this.lastName != null) user.setLastName(this.lastName);
        if (this.avatarUrl != null) user.setAvatarUrl(this.avatarUrl);
        if (this.profileDescription != null) user.setProfileDescription(this.profileDescription);
        if (this.isPrivate != null) user.setPrivateProfile(this.isPrivate);
        if (this.dateOfBirth != null) user.setDateOfBirth(this.dateOfBirth);
    }
}
