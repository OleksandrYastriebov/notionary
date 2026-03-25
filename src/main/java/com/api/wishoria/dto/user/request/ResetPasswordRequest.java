package com.api.wishoria.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for setting a new password using a reset token")
public record ResetPasswordRequest(

        @Schema(
                description = "The UUID token sent to the user's email",
                example = "123e4567-e89b-12d3-a456-426614174000",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Token is required")
        String token,

        @Schema(
                description = "The new password to set (must be at least 8 characters long)",
                example = "newStrongP@ssw0rd",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String newPassword
) {
}
