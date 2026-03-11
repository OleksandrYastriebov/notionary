package com.api.notionary.security.interceptop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public enum RateLimitPlan {
    AUTH(20, Duration.ofMinutes(1)),
    MUTATION(50, Duration.ofMinutes(1)),
    DEFAULT(100, Duration.ofMinutes(1));

    private final int capacity;
    private final Duration duration;
}
