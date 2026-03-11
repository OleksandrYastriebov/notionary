package com.api.notionary.controller;

import com.api.notionary.dto.payload.request.user.LogOutRequest;
import com.api.notionary.dto.payload.request.user.SignInRequest;
import com.api.notionary.dto.payload.request.user.SignUpRequest;
import com.api.notionary.dto.payload.request.token.TokenRefreshRequest;
import com.api.notionary.dto.token.JwtDto;
import com.api.notionary.dto.ApiResponseWrapper;
import com.api.notionary.dto.token.TokenRefreshDto;
import com.api.notionary.entity.User;
import com.api.notionary.service.AuthenticationService;
import com.api.notionary.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class UserAuthenticationController {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping(path = "/sign-up")
    public ResponseEntity<ApiResponseWrapper> signUp(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.ok(authenticationService.signUp(request));
    }

    @PostMapping(path = "/sign-in")
    public ResponseEntity<JwtDto> signIn(@Valid @RequestBody SignInRequest request) {
        return ResponseEntity.ok(authenticationService.signIn(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenRefreshDto> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<ApiResponseWrapper> logoutUser(@Valid @RequestBody LogOutRequest request,
                                        @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.ok(new ApiResponseWrapper("User already logged out or not authenticated."));
        }

        refreshTokenService.deleteByToken(request.getRefreshToken());
        return ResponseEntity.ok(new ApiResponseWrapper("Log out successful!"));
    }
}
