package com.api.notionary.dto.payload.request.user;

import com.api.notionary.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

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
        String avatarUrl
) {
    public void updateEntity(User user) {
        if (this.firstName != null) user.setFirstName(this.firstName);
        if (this.lastName != null) user.setLastName(this.lastName);
        if (this.avatarUrl != null) user.setAvatarUrl(this.avatarUrl);
    }
}
