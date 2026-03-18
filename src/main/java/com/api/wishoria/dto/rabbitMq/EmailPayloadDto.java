package com.api.wishoria.dto.rabbitMq;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record EmailPayloadDto(

        @NotBlank(message = "Email to is required")
        @Email(message = "Invalid email format")
        String to,

        @NotBlank(message = "Email Subject is required")
        @Size(max = 255)
        String subject,

        @NotBlank(message = "Email Content is required")
        String htmlContent

) implements Serializable {
}