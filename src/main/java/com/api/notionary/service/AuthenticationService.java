package com.api.notionary.service;

import com.api.notionary.dto.payload.request.user.SignInRequest;
import com.api.notionary.dto.payload.request.user.SignUpRequest;
import com.api.notionary.dto.payload.request.token.TokenRefreshRequest;
import com.api.notionary.dto.token.JwtDto;
import com.api.notionary.dto.ApiResponseWrapper;
import com.api.notionary.dto.token.TokenRefreshDto;
import com.api.notionary.entity.ConfirmationToken;
import com.api.notionary.entity.RefreshToken;
import com.api.notionary.entity.User;
import com.api.notionary.exception.TokenExpiredException;
import com.api.notionary.exception.TokenRefreshException;
import com.api.notionary.exception.UserAlreadyActivatedException;
import com.api.notionary.service.email.EmailSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class AuthenticationService {
    @Value("${app.url.backend}")
    private String appUrl;

    private final EmailSenderService emailSenderService;
    private final UserService userService;
    private final ConfirmationTokenService confirmationTokenService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public ApiResponseWrapper signUp(SignUpRequest request) {
        User user = request.toEntity();
        String confirmationToken = userService.signUpUser(user);
        String activationLink = String.format("%s/api/confirm-email?token=%s", appUrl, confirmationToken);
        emailSenderService.sendConfirmationEmail(user.getEmail(), user.getFirstName(), activationLink);

        return new ApiResponseWrapper("User registered successfully. Please check your email to activate your account.");
    }

    @Transactional
    public JwtDto signIn(SignInRequest signInRequest) {
        String userEmail = signInRequest.getEmail();
        String userPassword = signInRequest.getPassword();

        var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userEmail, userPassword));

        User user = (User) authentication.getPrincipal();

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();

        return new JwtDto(jwtToken, refreshToken, user.getId(), userEmail);
    }

    @Transactional
    public TokenRefreshDto refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));

        refreshTokenService.verifyExpiration(refreshToken);
        User user = refreshToken.getUser();
        refreshTokenService.deleteByToken(requestRefreshToken);

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());
        String jwt = jwtService.generateToken(user);

        return new TokenRefreshDto(jwt, newRefreshToken.getToken());
    }

    @Transactional
    public ApiResponseWrapper confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token)
                .orElseThrow(() -> new UserAlreadyActivatedException("Email already confirmed or Token is outdated."));

        User user = confirmationToken.getUser();

        if (Boolean.TRUE.equals(user.getEnabled())) {
            throw new UserAlreadyActivatedException("Email is already confirmed. You can log in now.");
        }

        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Confirmation token expired. Please request a new one.");
        }

        user.setEnabled(true);
        confirmationTokenService.deleteTokenFromDatabase(confirmationToken);
        return new ApiResponseWrapper("Account is successfully activated.");
    }

}
