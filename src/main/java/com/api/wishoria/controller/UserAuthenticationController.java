package com.api.wishoria.controller;

import com.api.wishoria.dto.payload.request.user.SignInRequest;
import com.api.wishoria.dto.payload.request.user.SignUpRequest;
import com.api.wishoria.dto.token.AuthResultDto;
import com.api.wishoria.dto.token.JwtDto;
import com.api.wishoria.dto.ApiResponseWrapper;
import com.api.wishoria.dto.token.TokenRefreshDto;
import com.api.wishoria.exception.TokenRefreshException;
import com.api.wishoria.security.interceptor.RateLimitPlan;
import com.api.wishoria.security.interceptor.RateLimited;
import com.api.wishoria.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
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

    @Value("${token.refresh.expiration.sec}")
    private int refreshCookieMaxAge;

    @Operation(summary = "Register a new user", description = "Creates a new user account and sends a confirmation email.")
    @PostMapping(path = "/sign-up")
    public ResponseEntity<ApiResponseWrapper> signUp(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.ok(authenticationService.signUp(request));
    }

    @Operation(summary = "Sign in", description = "Authenticates a user and returns JWT and Refresh tokens.")
    @PostMapping(path = "/sign-in")
    public ResponseEntity<JwtDto> signIn(@Valid @RequestBody SignInRequest request) {
        AuthResultDto authResult = authenticationService.signIn(request);

        ResponseCookie refreshCookie = createRefreshCookie(authResult.refreshToken(), refreshCookieMaxAge);
        JwtDto responseBody = new JwtDto(authResult.accessToken(), authResult.userId(), authResult.email());

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshCookie.toString()).body(responseBody);
    }

    @Operation(summary = "Refresh token", description = "Exchanges a valid Refresh Token (from cookie) for a new Access Token.")
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenRefreshDto> refreshToken(@CookieValue(name = "refreshToken", required = false)
                                                        String refreshTokenCookie) {

        if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
            throw new TokenRefreshException("Refresh Token is missing in cookies");
        }

        AuthResultDto authResult = authenticationService.refreshToken(refreshTokenCookie);

        ResponseCookie newRefreshCookie = createRefreshCookie(authResult.refreshToken(), refreshCookieMaxAge);
        TokenRefreshDto responseBody = new TokenRefreshDto(authResult.accessToken());

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, newRefreshCookie.toString()).body(responseBody);
    }

    @Operation(summary = "Log out", description = "Invalidates the current refresh token and clears the cookie.")
    @PostMapping("/sign-out")
    public ResponseEntity<ApiResponseWrapper> logoutUser(@CookieValue(name = "refreshToken", required = false)
                                                         String refreshTokenCookie) {

        authenticationService.logout(refreshTokenCookie);
        ResponseCookie cleanRefreshCookie = createRefreshCookie("", 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanRefreshCookie.toString())
                .body(new ApiResponseWrapper("Log out successful!"));
    }

    private ResponseCookie createRefreshCookie(String value, int maxAge) {
        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .secure(true)
                .path("/api/v1")
                .maxAge(maxAge)
                .sameSite("None")
                .build();
    }
}
