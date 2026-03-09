package com.api.notionary.dto.payload.request;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SignUpRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
    private LocalDateTime createdAt;

}
