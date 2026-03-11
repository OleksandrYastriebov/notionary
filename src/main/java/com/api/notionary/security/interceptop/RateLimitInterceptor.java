package com.api.notionary.security.interceptop;

import com.api.notionary.exception.RateLimitExceededException;
import com.api.notionary.service.RateLimitService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        String ip = getClientIP(request);
        RateLimitPlan plan = determineRateLimitPlan(handlerMethod);
        Bucket bucket = rateLimitService.resolveBucket(ip, plan);

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            log.warn("Rate limit exceeded for IP: {} on path: {} (Plan: {})", ip, request.getRequestURI(), plan);
            throw new RateLimitExceededException("Too many requests. Please try again later.");
        }
    }

    private RateLimitPlan determineRateLimitPlan(HandlerMethod handlerMethod) {
        RateLimited methodAnnotation = handlerMethod.getMethodAnnotation(RateLimited.class);
        if (methodAnnotation != null) {
            return methodAnnotation.action();
        }
        RateLimited classAnnotation = handlerMethod.getBeanType().getAnnotation(RateLimited.class);
        if (classAnnotation != null) {
            return classAnnotation.action();
        }
        return RateLimitPlan.DEFAULT;
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}