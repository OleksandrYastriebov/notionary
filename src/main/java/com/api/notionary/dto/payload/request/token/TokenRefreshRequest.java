package com.api.notionary.dto.payload.request.token;

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
public class TokenRefreshRequest {

    @NotBlank(message = "Refresh token is required for logout")
    @Size(min = 1, max = 50, message = "The input is too long. Max 50 characters.")
    private String refreshToken;

}
