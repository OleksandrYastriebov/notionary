package com.api.notionary.service;

import com.api.notionary.dto.payload.request.SignInRequest;
import com.api.notionary.dto.payload.request.SignUpRequest;
import com.api.notionary.dto.payload.request.TokenRefreshRequest;
import com.api.notionary.dto.payload.response.JwtResponse;
import com.api.notionary.dto.payload.response.TokenRefreshResponse;
import com.api.notionary.dto.ApiResponse;
import com.api.notionary.entity.ConfirmationToken;
import com.api.notionary.entity.RefreshToken;
import com.api.notionary.entity.User;
import com.api.notionary.entity.UserRole;
import com.api.notionary.exception.TokenRefreshException;
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
    private final TokenConfirmationService tokenConfirmationService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public ApiResponse signUp(SignUpRequest request) {

        User user = new User(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword(),
                LocalDateTime.now(),
                UserRole.USER);

        String confirmationToken = userService.signUpUser(user);
        String activationLink = String.format("%s/api/confirm-email?token=%s", appUrl, confirmationToken);
        emailSenderService.sendConfirmationEmail(user.getEmail(), user.getFirstName(), activationLink);

        return new ApiResponse("User registered successfully. Please check your email to activate your account.");
    }

    public JwtResponse signIn(SignInRequest signInRequest) {
        String userEmail = signInRequest.getEmail();
        String userPassword = signInRequest.getPassword();

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userEmail, userPassword));

        User user = userService.findByEmail(userEmail);

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();

        return new JwtResponse(jwtToken, refreshToken, user.getId(), userEmail);
    }

    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtService.generateToken(user);
                    return new TokenRefreshResponse(token, requestRefreshToken);
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    @Transactional
    public ApiResponse confirmToken(String token) {
        ConfirmationToken confirmationToken = tokenConfirmationService
                .getToken(token)
                .orElseThrow(() ->
                        new IllegalStateException("Email already confirmed or Token is outdated."));

        if (userService.isUserEnabled(confirmationToken.getUser().getEmail())) {
            throw new IllegalStateException("Email already confirmed.");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Confirmation token expired.");
        }

        userService.enableAppUser(confirmationToken.getUser().getEmail());
        tokenConfirmationService.deleteTokenFromDatabase(token);
        return new ApiResponse("Account activated");
    }

}
