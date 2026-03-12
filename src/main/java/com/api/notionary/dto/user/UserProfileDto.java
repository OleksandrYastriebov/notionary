package com.api.notionary.dto.user;

import com.api.notionary.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "User profile information")
public record UserProfileDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        LocalDateTime createdAt
) {
    public UserProfileDto(User user) {
        this(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
