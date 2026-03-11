package com.api.notionary.dto.payload.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogOutRequest {

    @NotBlank(message = "Refresh token is required for logout")
    @Size(min = 1, max = 50, message = "The input is too long")
    private String refreshToken;

}