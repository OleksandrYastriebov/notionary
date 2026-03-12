package com.api.notionary.service;

import com.api.notionary.security.interceptor.RateLimitPlan;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private static final int MAXIMUM_CACHE_HOLDER_SIZE = 100_000;
    private static final int IN_MEMORY_CACHE_DURATION = 1;

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .maximumSize(MAXIMUM_CACHE_HOLDER_SIZE)
            .expireAfterAccess(Duration.ofHours(IN_MEMORY_CACHE_DURATION))
            .build();

    public Bucket resolveBucket(String ip, RateLimitPlan plan) {
        String key = ip + "_" + plan.name();
        return buckets.get(key, k -> createNewBucket(plan));
    }

    private Bucket createNewBucket(RateLimitPlan plan) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(plan.getCapacity())
                .refillGreedy(plan.getCapacity(), plan.getDuration())
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
