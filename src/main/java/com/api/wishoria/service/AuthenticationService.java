package com.api.wishoria.service;

import com.api.wishoria.dto.user.request.SignInRequest;
import com.api.wishoria.dto.user.request.SignUpRequest;
import com.api.wishoria.dto.token.AuthResultDto;
import com.api.wishoria.dto.ApiResponseWrapper;
import com.api.wishoria.entity.ConfirmationToken;
import com.api.wishoria.entity.RefreshToken;
import com.api.wishoria.entity.User;
import com.api.wishoria.event.UserRegisteredEvent;
import com.api.wishoria.exception.EntityNotFoundException;
import com.api.wishoria.exception.TokenExpiredException;
import com.api.wishoria.exception.TokenRefreshException;
import com.api.wishoria.exception.UserAlreadyActivatedException;
import com.api.wishoria.repository.UserRepository;
import com.api.wishoria.util.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Instant;

@RequiredArgsConstructor
@Service
public class AuthenticationService {

    private static final String EMAIL_CONFIRMED_LOG_IN = "Email is already confirmed. You can Log In now.";

    private final ApplicationEventPublisher eventPublisher;
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

        eventPublisher.publishEvent(new UserRegisteredEvent(this, user, confirmationToken));

        return new ApiResponseWrapper("User registered successfully. Please check your email to activate your account.");
    }

    @Transactional
    public AuthResultDto signIn(SignInRequest request) {
        String userEmail = EmailNormalizer.normalize(request.email());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userEmail, request.password())
        );

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with email: %s not found", userEmail)));

        String jwt = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResultDto(jwt, refreshToken.getToken(), user.getId(), userEmail);
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

        return new AuthResultDto(newJwt, newRefreshToken.getToken(), user.getId(), EmailNormalizer.normalize(user.getEmail()));
    }

    @Transactional
    public ApiResponseWrapper confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.getToken(token)
                .orElseThrow(() -> new UserAlreadyActivatedException("Email is already confirmed or Token is invalid or outdated."));

        User user = confirmationToken.getUser();

        if (user.isEnabled()) {
            throw new UserAlreadyActivatedException(EMAIL_CONFIRMED_LOG_IN);
        }

        if (confirmationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenExpiredException("Confirmation token expired. Please request a new one.");
        }

        user.setEnabled(true);
        confirmationTokenService.deleteTokenFromDatabase(confirmationToken);

        return new ApiResponseWrapper("Account is successfully activated.");
    }

    @Transactional
    public ApiResponseWrapper resendConfirmationEmail(String email) {
        User user = userService.getUserByEmail(EmailNormalizer.normalize(email));

        if (user.isEnabled()) {
            throw new UserAlreadyActivatedException(EMAIL_CONFIRMED_LOG_IN);
        }

        String newToken = userService.generateNewConfirmationToken(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(this, user, newToken));

        return new ApiResponseWrapper("A new confirmation email has been sent. Please check your inbox.");
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.deleteByToken(refreshToken);
        }
    }

}
