package com.api.wishoria.controller;

import com.api.wishoria.dto.ApiResponseWrapper;
import com.api.wishoria.dto.user.request.ResendConfirmationTokenRequest;
import com.api.wishoria.security.interceptor.RateLimitPlan;
import com.api.wishoria.security.interceptor.RateLimited;
import com.api.wishoria.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Tag(name = "Email Confirmation", description = "Endpoints for handling email verification")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
@RateLimited(action = RateLimitPlan.EMAIL)
public class EmailConfirmationController {

    @Value("${app.url.frontend}")
    private String frontendUrl;

    private final AuthenticationService authenticationService;

    @Operation(summary = "Confirm email", description = "Activates a user account using the token sent via email.")
    @GetMapping(path = "/confirm-email")
    public ModelAndView confirmEmail(@Parameter(description = "The confirmation token from the email link")
                                     @RequestParam("token") String token) {

        ModelAndView modelAndView = new ModelAndView("email-confirmed");
        modelAndView.addObject("frontendUrl", frontendUrl);

        try {
            authenticationService.confirmToken(token);
            modelAndView.addObject("success", true);
            modelAndView.addObject("message", "Your account has been successfully activated. You can now log in!");
        } catch (Exception e) {
            modelAndView.addObject("success", false);
            modelAndView.addObject("message", e.getMessage());
        }

        return modelAndView;
    }

    @Operation(summary = "Resend confirmation email", description = "Generates a new token and resends the activation email.")
    @PostMapping(path = "/resend-confirmation-email")
    public ResponseEntity<ApiResponseWrapper> resendConfirmationEmail(@Valid @RequestBody ResendConfirmationTokenRequest request) {
        return ResponseEntity.ok(authenticationService.resendConfirmationEmail(request.email()));
    }
}