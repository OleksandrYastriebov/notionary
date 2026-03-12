package com.api.notionary.dto.payload.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for user authentication")
public record SignInRequest(
        @Schema(description = "User email", example = "john.doe@notionary.app", requiredMode = Schema.RequiredMode.REQUIRED)
        @Email(message = "Email format is invalid")
        @NotBlank(message = "Email can not be empty")
        @Size(max = 100, message = "The input is too long. Max 100 characters.")
        String email,

        @Schema(description = "User password", example = "SecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password can not be empty")
        @Size(max = 100, message = "The input is too long. Max 100 characters.")
        String password
) {
}
