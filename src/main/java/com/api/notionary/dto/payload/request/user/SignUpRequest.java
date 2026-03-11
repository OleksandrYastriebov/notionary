package com.api.notionary.dto.payload.request.user;

import com.api.notionary.entity.User;
import com.api.notionary.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {

    private static final String THE_INPUT_IS_TOO_LONG = "The input is too long. Max 50 characters.";

    @NotBlank(message = "First Name cannot be empty")
    @Size(max = 50, message = THE_INPUT_IS_TOO_LONG)
    private String firstName;

    @NotBlank(message = "Last Name cannot be empty")
    @Size(max = 50, message = THE_INPUT_IS_TOO_LONG)
    private String lastName;

    @Email(message = "Email format is invalid")
    @NotBlank(message = "Email can not be empty")
    @Size(max = 100, message = "The input is too long. Max 100 characters.")
    private String email;

    @NotBlank(message = "Password can not be empty")
    @Size(min = 8, max = 100, message = "Password must be at least 8 but not longer than 100 characters")
    private String password;

    public User toEntity() {
        return new User(
                this.firstName,
                this.lastName,
                this.email,
                this.password,
                LocalDateTime.now(),
                UserRole.ROLE_USER
        );
    }
}
