package com.api.wishoria.dto.payload.request.user;

import com.api.wishoria.entity.User;
import com.api.wishoria.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Schema(description = "Request payload for creating a new user account")
public record SignUpRequest(
        @Schema(description = "User's first name", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "First Name cannot be empty")
        @Size(max = 50, message = "The input is too long. Max 50 characters.")
        String firstName,

        @Schema(description = "User's last name", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Last Name cannot be empty")
        @Size(max = 50, message = "The input is too long. Max 50 characters.")
        String lastName,

        @Schema(description = "Valid email address", example = "john.doe@wishoria.app", requiredMode = Schema.RequiredMode.REQUIRED)
        @Email(message = "Email format is invalid")
        @NotBlank(message = "Email can not be empty")
        @Size(max = 100, message = "The input is too long. Max 100 characters.")
        String email,

        @Schema(description = "Strong password (min 8 chars)", example = "SecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password can not be empty")
        @Size(min = 8, max = 100, message = "Password must be at least 8 but not longer than 100 characters")
        String password
) {
    public User toEntity() {
        return new User(
                this.firstName,
                this.lastName,
                this.email.toLowerCase().trim(),
                this.password,
                Instant.now(),
                UserRole.ROLE_USER);
    }
}