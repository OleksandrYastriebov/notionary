package com.api.notionary.service;

import com.api.notionary.security.interceptor.RateLimitPlan;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    private final RateLimitService rateLimitService = new RateLimitService();

    @Test
    void resolveBucket_shouldReturnBucket_forGivenIpAndPlan() {
        Bucket bucket = rateLimitService.resolveBucket("192.168.1.1", RateLimitPlan.AUTH);

        assertThat(bucket).isNotNull();
        assertThat(bucket.tryConsume(1)).isTrue();
    }

    @Test
    void resolveBucket_shouldReturnSameBucket_forSameKey() {
        Bucket b1 = rateLimitService.resolveBucket("10.0.0.1", RateLimitPlan.DEFAULT);
        Bucket b2 = rateLimitService.resolveBucket("10.0.0.1", RateLimitPlan.DEFAULT);

        assertThat(b1).isSameAs(b2);
    }

    @Test
    void resolveBucket_shouldReturnDifferentBuckets_forDifferentPlans() {
        Bucket authBucket = rateLimitService.resolveBucket("127.0.0.1", RateLimitPlan.AUTH);
        Bucket emailBucket = rateLimitService.resolveBucket("127.0.0.1", RateLimitPlan.EMAIL);

        assertThat(authBucket).isNotSameAs(emailBucket);
    }

    @Test
    void resolveBucket_shouldReturnDifferentBuckets_forDifferentIps() {
        Bucket b1 = rateLimitService.resolveBucket("1.2.3.4", RateLimitPlan.MUTATION);
        Bucket b2 = rateLimitService.resolveBucket("5.6.7.8", RateLimitPlan.MUTATION);

        assertThat(b1).isNotSameAs(b2);
    }

    @Test
    void resolveBucket_shouldRespectPlanCapacity() {
        Bucket bucket = rateLimitService.resolveBucket("ip", RateLimitPlan.EMAIL);
        assertThat(bucket.tryConsume(5)).isTrue();
        assertThat(bucket.tryConsume(1)).isFalse();
    }
}
