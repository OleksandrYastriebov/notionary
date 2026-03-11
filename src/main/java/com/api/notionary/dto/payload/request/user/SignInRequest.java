package com.api.notionary.dto.payload.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignInRequest {

    private static final String THE_INPUT_IS_TOO_LONG = "The input is too long. Max 100 characters.";

    @Email(message = "Email format is invalid")
    @NotBlank(message = "Email can not be empty")
    @Size(max = 100, message = THE_INPUT_IS_TOO_LONG)
    private String email;

    @NotBlank(message = "Password can not be empty")
    @Size(max = 100, message = THE_INPUT_IS_TOO_LONG)
    private String password;

}
