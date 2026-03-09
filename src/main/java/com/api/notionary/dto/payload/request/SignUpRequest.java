package com.api.notionary.dto.payload.request;

import jakarta.validation.constraints.Email;
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
    @Email(message = "Email is not valid")
    private String email;
    private String username;
    private String password;
    private LocalDateTime createdAt;

}
