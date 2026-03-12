package com.api.notionary.dto.payload.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload to resend the account activation email")
public record ResendConfirmationTokenRequest(
        @Schema(description = "User's registered email address",
                example = "user@notionary.app",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email can not be empty")
        @Email(message = "Invalid email format")
        String email
) {
}