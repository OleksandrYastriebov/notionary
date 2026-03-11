package com.api.notionary.dto.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtDto {

    private String jwtToken;
    private String type = "Bearer";
    private String refreshToken;
    private Long id;
    private String email;

    public JwtDto(String jwtToken, String refreshToken, Long id, String email) {
        this.jwtToken = jwtToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.email = email;
    }
}
