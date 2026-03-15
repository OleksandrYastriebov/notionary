package com.api.notionary.controller;

import com.api.notionary.dto.ApiResponseWrapper;
import com.api.notionary.dto.payload.request.user.ResendConfirmationTokenRequest;
import com.api.notionary.exception.GlobalExceptionHandler;
import com.api.notionary.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmailConfirmationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private EmailConfirmationController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final String FRONTEND_URL = "http://localhost:3000";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "frontendUrl", FRONTEND_URL);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void confirmEmail_whenTokenValid_shouldSetSuccessAttributes() {
        String token = "valid-token";

        ModelAndView mav = controller.confirmEmail(token);

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
    void confirmEmail_viaHttp_whenTokenParamMissing_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/confirm-email"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmEmail_viaHttp_whenTokenValid_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/confirm-email").param("token", "some-token"))
                .andExpect(status().isOk());

        verify(authenticationService).confirmToken("some-token");
    }

    @Test
    void resendConfirmationEmail_whenValidEmail_shouldReturnOkWithMessage() throws Exception {
        String email = "user@notionary.app";
        when(authenticationService.resendConfirmationEmail(email))
                .thenReturn(new ApiResponseWrapper("Activation email has been resent"));

        ResendConfirmationTokenRequest request = new ResendConfirmationTokenRequest(email);

        mockMvc.perform(post("/api/v1/resend-confirmation-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Activation email has been resent"));

        verify(authenticationService).resendConfirmationEmail(email);
    }

    @Test
    void resendConfirmationEmail_shouldDelegateToServiceAndWrapResponse() {
        String email = "user@notionary.app";
        ResendConfirmationTokenRequest request = new ResendConfirmationTokenRequest(email);
        ApiResponseWrapper serviceResponse = new ApiResponseWrapper("Activation email has been resent");
        when(authenticationService.resendConfirmationEmail(email)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponseWrapper> response = controller.resendConfirmationEmail(request);

        verify(authenticationService).resendConfirmationEmail(email);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Activation email has been resent");
    }

    @Test
    void resendConfirmationEmail_whenBodyMissing_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/resend-confirmation-email")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
