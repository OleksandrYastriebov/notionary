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

    @NotBlank(message = "First Name cannot be empty")
    private String firstName;

    @NotBlank(message = "Last Name cannot be empty")
    private String lastName;

    @Email(message = "Email format is invalid")
    @NotBlank(message = "Email can not be empty")
    private String email;

    @NotBlank(message = "Password can not be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
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
