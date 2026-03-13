package com.api.notionary.dto.token;

public record AuthResultDto(
        String accessToken,
        String refreshToken,
        Long userId,
        String email
) {}