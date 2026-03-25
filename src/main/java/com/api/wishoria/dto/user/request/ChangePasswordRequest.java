package com.api.wishoria.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for changing user password")
public record ChangePasswordRequest(

        @Schema(description = "Current password", example = "OldP@ssw0rd")
        @NotBlank(message = "Current password cannot be empty")
        String currentPassword,

        @Schema(description = "New password", example = "NewStr0ngP@ss!")
        @NotBlank(message = "New password cannot be empty")
        @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters")
        String newPassword
) {
}
