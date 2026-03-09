package com.api.notionary.controller;

import com.api.notionary.dto.payload.request.SignInRequest;
import com.api.notionary.dto.payload.request.SignUpRequest;
import com.api.notionary.dto.payload.request.TokenRefreshRequest;
import com.api.notionary.dto.payload.response.JwtResponse;
import com.api.notionary.dto.payload.response.TokenRefreshResponse;
import com.api.notionary.entity.ApiResponse;
import com.api.notionary.entity.User;
import com.api.notionary.service.AuthenticationService;
import com.api.notionary.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserAuthenticationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserAuthenticationController.class);

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public UserAuthenticationController(AuthenticationService authenticationService, RefreshTokenService refreshTokenService) {
        this.authenticationService = authenticationService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping(path = "/sign-up")
    public ResponseEntity<ApiResponse> singUp(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.ok(authenticationService.signUp(request));
    }

    @PostMapping(path = "/sign-in")
    public ResponseEntity<JwtResponse> singIn(@RequestBody SignInRequest request) {
        return ResponseEntity.ok(authenticationService.signIn(request));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        LOGGER.info("Trying to refresh token. Request: {}", request);
        return ResponseEntity.ok(authenticationService.refresh(request));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<?> logoutUser(Authentication authentication) {
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.ok(new ApiResponse("User already logged out or not authenticated."));
        }

        User userDetails = (User) authentication.getPrincipal();
        Long userId = userDetails.getId();
        refreshTokenService.deleteByUserId(userId);
        return ResponseEntity.ok(new ApiResponse("Log out successful!"));
    }
}
