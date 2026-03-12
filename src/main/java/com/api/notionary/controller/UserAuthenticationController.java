package com.api.notionary.controller;

import com.api.notionary.dto.payload.request.user.LogOutRequest;
import com.api.notionary.dto.payload.request.user.SignInRequest;
import com.api.notionary.dto.payload.request.user.SignUpRequest;
import com.api.notionary.dto.payload.request.token.TokenRefreshRequest;
import com.api.notionary.dto.token.JwtDto;
import com.api.notionary.dto.ApiResponseWrapper;
import com.api.notionary.dto.token.TokenRefreshDto;
import com.api.notionary.entity.User;
import com.api.notionary.security.interceptor.RateLimitPlan;
import com.api.notionary.security.interceptor.RateLimited;
import com.api.notionary.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Endpoints for user registration, login, and token management")
@RequiredArgsConstructor
@Slf4j
@RestController
@RateLimited(action = RateLimitPlan.AUTH)
@RequestMapping("/api/v1")
public class UserAuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Register a new user", description = "Creates a new user account and sends a confirmation email.")
    @PostMapping(path = "/sign-up")
    public ResponseEntity<ApiResponseWrapper> signUp(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.ok(authenticationService.signUp(request));
    }

    @Operation(summary = "Sign in", description = "Authenticates a user and returns JWT and Refresh tokens.")
    @PostMapping(path = "/sign-in")
    public ResponseEntity<JwtDto> signIn(@Valid @RequestBody SignInRequest request) {
        return ResponseEntity.ok(authenticationService.signIn(request));
    }

    @Operation(summary = "Refresh token", description = "Exchanges a valid refresh token for a new JWT access token.")
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenRefreshDto> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    @Operation(summary = "Log out", description = "Invalidates the current refresh token.")
    @PostMapping("/sign-out")
    public ResponseEntity<ApiResponseWrapper> logoutUser(@Valid @RequestBody LogOutRequest request,
                                                         @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(authenticationService.logout(request.refreshToken(), user));
    }
}
