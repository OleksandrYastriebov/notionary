package com.api.notionary.controller;

import com.api.notionary.dto.payload.request.user.LogOutRequest;
import com.api.notionary.dto.payload.request.user.SignInRequest;
import com.api.notionary.dto.payload.request.user.SignUpRequest;
import com.api.notionary.dto.payload.request.token.TokenRefreshRequest;
import com.api.notionary.dto.token.JwtDto;
import com.api.notionary.dto.ApiResponseWrapper;
import com.api.notionary.dto.token.TokenRefreshDto;
import com.api.notionary.entity.User;
import com.api.notionary.exception.TokenRefreshException;
import com.api.notionary.security.interceptor.RateLimitPlan;
import com.api.notionary.security.interceptor.RateLimited;
import com.api.notionary.service.AuthenticationService;
import com.api.notionary.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final RefreshTokenService refreshTokenService;

    private final int refreshCookieMaxAge = 7 * 24 * 60 * 60;

    @Operation(summary = "Register a new user", description = "Creates a new user account and sends a confirmation email.")
    @PostMapping(path = "/sign-up")
    public ResponseEntity<ApiResponseWrapper> signUp(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.ok(authenticationService.signUp(request));
    }

    @Operation(summary = "Sign in", description = "Authenticates a user and returns JWT and Refresh tokens.")
    @PostMapping(path = "/sign-in")
    public ResponseEntity<JwtDto> signIn(@Valid @RequestBody SignInRequest request) {
        JwtDto jwtDto = authenticationService.signIn(request);
        ResponseCookie refreshCookie = createRefreshCookie(jwtDto.refreshToken(), refreshCookieMaxAge);
        JwtDto responseBody = new JwtDto(jwtDto.jwtToken(), "Bearer", null, jwtDto.id(), jwtDto.email());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(responseBody);
    }

    @Operation(summary = "Refresh token", description = "Exchanges a valid Refresh Token (from cookie) for a new Access Token.")
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenRefreshDto> refreshToken(@CookieValue(name = "refreshToken", required = false)
                                                        String refreshTokenCookie) {

        if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
            throw new TokenRefreshException(null, "Refresh Token is missing in cookies");
        }

        TokenRefreshDto refreshDto = authenticationService.refreshToken(refreshTokenCookie);
        ResponseCookie newRefreshCookie = createRefreshCookie(refreshDto.refreshToken(), refreshCookieMaxAge);

        TokenRefreshDto responseBody = new TokenRefreshDto(refreshDto.accessToken(), null, "Bearer");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newRefreshCookie.toString())
                .body(responseBody);
    }

    @Operation(summary = "Log out", description = "Invalidates the current refresh token and clears the cookie.")
    @PostMapping("/sign-out")
    public ResponseEntity<ApiResponseWrapper> logoutUser(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie) {

        if (refreshTokenCookie != null && !refreshTokenCookie.isBlank()) {
            refreshTokenService.deleteByToken(refreshTokenCookie);
        }
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
