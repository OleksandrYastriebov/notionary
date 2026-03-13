package com.api.notionary.service;

import com.api.notionary.dto.payload.request.user.SignInRequest;
import com.api.notionary.dto.payload.request.user.SignUpRequest;
import com.api.notionary.dto.token.AuthResultDto;
import com.api.notionary.dto.token.JwtDto;
import com.api.notionary.dto.ApiResponseWrapper;
import com.api.notionary.dto.token.TokenRefreshDto;
import com.api.notionary.entity.ConfirmationToken;
import com.api.notionary.entity.RefreshToken;
import com.api.notionary.entity.User;
import com.api.notionary.exception.EntityNotFoundException;
import com.api.notionary.exception.TokenExpiredException;
import com.api.notionary.exception.TokenRefreshException;
import com.api.notionary.exception.UserAlreadyActivatedException;
import com.api.notionary.repository.UserRepository;
import com.api.notionary.service.email.EmailSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class AuthenticationService {

    private static final String EMAIL_CONFIRMED_LOG_IN = "Email is already confirmed. You can log in now.";

    @Value("${app.url.backend}")
    private String appUrl;

    private final EmailSenderService emailSenderService;
    private final UserService userService;
    private final ConfirmationTokenService confirmationTokenService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Transactional
    public ApiResponseWrapper signUp(SignUpRequest request) {
        User user = request.toEntity();
        String confirmationToken = userService.signUpUser(user);

        sendActivationEmail(user, confirmationToken);

        return new ApiResponseWrapper("User registered successfully. Please check your email to activate your account.");
    }

    @Transactional
    public AuthResultDto signIn(SignInRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with email: %s not found", request.email())));

        String jwt = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResultDto(jwt, refreshToken.getToken(), user.getId(), user.getEmail());
    }

    @Transactional
    public AuthResultDto refreshToken(String requestRefreshToken) {
        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .orElseThrow(() -> new TokenRefreshException("Refresh token is not in database!"));

        refreshTokenService.verifyExpiration(refreshToken);
        User user = refreshToken.getUser();

        refreshTokenService.deleteByToken(requestRefreshToken);

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());
        String newJwt = jwtService.generateToken(user);

        return new AuthResultDto(newJwt, newRefreshToken.getToken(), user.getId(), user.getEmail());
    }

    @Transactional
    public ApiResponseWrapper confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.getToken(token)
                .orElseThrow(() -> new UserAlreadyActivatedException("Email is already confirmed or Token is invalid or outdated."));

        User user = confirmationToken.getUser();

        if (user.isEnabled()) {
            throw new UserAlreadyActivatedException(EMAIL_CONFIRMED_LOG_IN);
        }

        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Confirmation token expired. Please request a new one.");
        }

        user.setEnabled(true);
        confirmationTokenService.deleteTokenFromDatabase(confirmationToken);

        return new ApiResponseWrapper("Account is successfully activated.");
    }

    @Transactional
    public ApiResponseWrapper resendConfirmationEmail(String email) {
        User user = userService.getUserByEmail(email);

        if (user.isEnabled()) {
            throw new UserAlreadyActivatedException(EMAIL_CONFIRMED_LOG_IN);
        }

        String newToken = userService.generateNewConfirmationToken(user);
        sendActivationEmail(user, newToken);

        return new ApiResponseWrapper("A new confirmation email has been sent. Please check your inbox.");
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.deleteByToken(refreshToken);
        }
    }

    private void sendActivationEmail(User user, String token) {
        String activationLink = String.format("%s/api/v1/confirm-email?token=%s", appUrl, token);
        emailSenderService.sendConfirmationEmail(user.getEmail(), user.getFirstName(), activationLink);
    }

}
