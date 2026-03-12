package com.api.notionary.dto.payload.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendConfirmationTokenRequest {

    @NotBlank(message = "Email can not be empty")
    @Email(message = "Invalid email format")
    private String email;

}