package com.api.wishoria.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String USERS_BY_EMAIL_CACHE = "users_by_email";
    public static final String SITEMAP_CACHE = "sitemap";
    public static final String AVAILABLE_WISHLISTS_CACHE = "available_wishlists";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.registerCustomCache(USERS_BY_EMAIL_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(1_000)
                        .expireAfterWrite(Duration.ofMinutes(30))
                        .build());

        cacheManager.registerCustomCache(SITEMAP_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(1)
                        .expireAfterWrite(Duration.ofMinutes(10))
                        .build());

        cacheManager.registerCustomCache(AVAILABLE_WISHLISTS_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(10_000)
                        .expireAfterWrite(Duration.ofMinutes(5))
                        .build());

        return cacheManager;
    }
}
