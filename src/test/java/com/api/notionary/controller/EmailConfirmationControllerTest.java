package com.api.notionary.controller;

import com.api.notionary.dto.ApiResponseWrapper;
import com.api.notionary.dto.payload.request.user.ResendConfirmationTokenRequest;
import com.api.notionary.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(MockitoExtension.class)
public class EmailConfirmationControllerTest {

    @Mock
    private AuthenticationService authenticationService;
    @InjectMocks
    private EmailConfirmationController controller;

    private static final String FRONTEND_URL = "http://localhost:3000";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "frontendUrl", FRONTEND_URL);
    }

    @Test
    void confirmEmail_whenTokenValid_shouldSetSuccessAttributes() {
        // given
        String token = "valid-token";
        // when
        ModelAndView mav = controller.confirmEmail(token);
        // then
        verify(authenticationService).confirmToken(token);
        assertThat(mav.getViewName()).isEqualTo("email-confirmed");
        assertThat(mav.getModel().get("frontendUrl")).isEqualTo(FRONTEND_URL);
        assertThat(mav.getModel().get("success")).isEqualTo(true);
        assertThat(mav.getModel().get("message"))
                .isEqualTo("Your account has been successfully activated. You can now log in!");
    }

    @Test
    void confirmEmail_whenServiceThrows_shouldSetErrorAttributes() {
        String token = "invalid-token";
        String errorMessage = "Token is invalid or expired";
        doThrow(new IllegalStateException(errorMessage))
                .when(authenticationService).confirmToken(token);

        ModelAndView mav = controller.confirmEmail(token);

        verify(authenticationService).confirmToken(token);
        assertThat(mav.getViewName()).isEqualTo("email-confirmed");
        assertThat(mav.getModel().get("frontendUrl")).isEqualTo(FRONTEND_URL);
        assertThat(mav.getModel().get("success")).isEqualTo(false);
        assertThat(mav.getModel().get("message")).isEqualTo(errorMessage);
    }

    @Test
    void resendConfirmationEmail_shouldDelegateToServiceAndWrapResponse() {
        String email = "user@notionary.app";
        ResendConfirmationTokenRequest request = new ResendConfirmationTokenRequest(email);
        ApiResponseWrapper serviceResponse =
                new ApiResponseWrapper("Activation email has been resent");
        when(authenticationService.resendConfirmationEmail(email))
                .thenReturn(serviceResponse);

        ResponseEntity<ApiResponseWrapper> response = controller.resendConfirmationEmail(request);

        verify(authenticationService).resendConfirmationEmail(email);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Activation email has been resent");
    }
}
