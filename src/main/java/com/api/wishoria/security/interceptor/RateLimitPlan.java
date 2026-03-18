package com.api.wishoria.security.interceptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public enum RateLimitPlan {
    EMAIL(5, Duration.ofMinutes(1)),
    AUTH(10, Duration.ofMinutes(1)),
    MUTATION(50, Duration.ofMinutes(1)),
    DEFAULT(100, Duration.ofMinutes(1));

    private final int capacity;
    private final Duration duration;
}
