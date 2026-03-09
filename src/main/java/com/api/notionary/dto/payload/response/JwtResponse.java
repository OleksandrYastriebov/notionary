package com.api.notionary.dto.payload.response;

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
public class JwtResponse {

    private String jwtToken;
    private String type = "Bearer";
    private String refreshToken;
    private Long id;
    private String email;

    public JwtResponse(String jwtToken, String refreshToken, Long id, String email) {
        this.jwtToken = jwtToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.email = email;
    }
}
