package com.api.notionary.dto.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class TokenRefreshRequest {

    @NotBlank(message = "*ERROR* 'RefreshToken' must be not blank or null")
    private String refreshToken;

}
