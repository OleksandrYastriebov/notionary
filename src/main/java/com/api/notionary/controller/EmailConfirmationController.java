package com.api.notionary.controller;

import com.api.notionary.dto.ApiResponseWrapper;
import com.api.notionary.security.interceptor.RateLimitPlan;
import com.api.notionary.security.interceptor.RateLimited;
import com.api.notionary.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
@RateLimited(action = RateLimitPlan.DEFAULT)
public class EmailConfirmationController {

    private final AuthenticationService authenticationService;

    @GetMapping(path = "/confirm-email")
    public ResponseEntity<ApiResponseWrapper> confirmEmail(@RequestParam("token") String token) {
        return ResponseEntity.ok(authenticationService.confirmToken(token));
    }
}
