package com.api.wishoria.controller;

import com.api.wishoria.dto.ApiResponseWrapper;
import com.api.wishoria.dto.payload.request.user.SignInRequest;
import com.api.wishoria.dto.payload.request.user.SignUpRequest;
import com.api.wishoria.dto.token.AuthResultDto;
import com.api.wishoria.dto.token.JwtDto;
import com.api.wishoria.dto.token.TokenRefreshDto;
import com.api.wishoria.exception.GlobalExceptionHandler;
import com.api.wishoria.exception.TokenRefreshException;
import com.api.wishoria.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private UserAuthenticationController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final int REFRESH_COOKIE_MAX_AGE = 3600;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "refreshCookieMaxAge", REFRESH_COOKIE_MAX_AGE);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void signUp_shouldReturnOkWithServiceResponse() {
        SignUpRequest request = new SignUpRequest("John", "Doe", "user@wishoria.app", "password123");
        ApiResponseWrapper serviceResponse = new ApiResponseWrapper("User registered successfully");

        when(authenticationService.signUp(any(SignUpRequest.class))).thenReturn(serviceResponse);

        ResponseEntity<ApiResponseWrapper> response = controller.signUp(request);

        verify(authenticationService).signUp(request);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("User registered successfully");
    }

    @Test
    void signUp_viaHttp_withValidRequest_shouldReturn200() throws Exception {
        SignUpRequest request = new SignUpRequest("John", "Doe", "john@wishoria.app", "password123");
        when(authenticationService.signUp(any(SignUpRequest.class)))
                .thenReturn(new ApiResponseWrapper("Check your email to confirm your account."));

        mockMvc.perform(post("/api/v1/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Check your email to confirm your account."));
    }

    @Test
    void signUp_viaHttp_withBlankFirstName_shouldReturn400() throws Exception {
        SignUpRequest request = new SignUpRequest("", "Doe", "john@wishoria.app", "password123");

        mockMvc.perform(post("/api/v1/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signUp_viaHttp_withInvalidEmail_shouldReturn400() throws Exception {
        SignUpRequest request = new SignUpRequest("John", "Doe", "not-an-email", "password123");

        mockMvc.perform(post("/api/v1/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signUp_viaHttp_withPasswordTooShort_shouldReturn400() throws Exception {
        SignUpRequest request = new SignUpRequest("John", "Doe", "john@wishorias.app", "short");

        mockMvc.perform(post("/api/v1/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signUp_viaHttp_withMissingBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/sign-up")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signIn_shouldReturnJwtDtoAndSetRefreshCookie() {
        SignInRequest request = new SignInRequest("user@wishoria.app", "password123");
        AuthResultDto authResult = new AuthResultDto("access-token-value", "refresh-token-value", 42L,
                "user@wishoria.app");

        when(authenticationService.signIn(any(SignInRequest.class))).thenReturn(authResult);

        ResponseEntity<JwtDto> response = controller.signIn(request);

        verify(authenticationService).signIn(request);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        JwtDto body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.jwtToken()).isEqualTo("access-token-value");
        assertThat(body.id()).isEqualTo(42L);
        assertThat(body.email()).isEqualTo("user@wishoria.app");

        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader).isNotNull();
        assertThat(setCookieHeader).contains("refreshToken=refresh-token-value");
    }

    @Test
    void signIn_viaHttp_withValidCredentials_shouldReturnJwtAndSetCookie() throws Exception {
        SignInRequest request = new SignInRequest("john@wishoria.app", "password123");
        AuthResultDto authResult = new AuthResultDto("access-token", "refresh-token", 42L, "john@wishoria.app");
        when(authenticationService.signIn(any(SignInRequest.class))).thenReturn(authResult);

        mockMvc.perform(post("/api/v1/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").value("access-token"))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.email").value("john@wishoria.app"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=refresh-token")));
    }

    @Test
    void signIn_viaHttp_withBlankPassword_shouldReturn400() throws Exception {
        SignInRequest request = new SignInRequest("john@wishoria.app", "");

        mockMvc.perform(post("/api/v1/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signIn_viaHttp_withInvalidEmailFormat_shouldReturn400() throws Exception {
        SignInRequest request = new SignInRequest("not-an-email", "password123");

        mockMvc.perform(post("/api/v1/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshToken_shouldExchangeCookieForNewTokens() {
        String refreshTokenCookie = "existing-refresh-token";
        AuthResultDto authResult = new AuthResultDto("new-access-token", "new-refresh-token", 42L,
                "user@wishoria.app");

        when(authenticationService.refreshToken(refreshTokenCookie)).thenReturn(authResult);

        ResponseEntity<TokenRefreshDto> response = controller.refreshToken(refreshTokenCookie);

        verify(authenticationService).refreshToken(refreshTokenCookie);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        TokenRefreshDto body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.accessToken()).isEqualTo("new-access-token");

        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader).isNotNull();
        assertThat(setCookieHeader).contains("refreshToken=new-refresh-token");
    }

    @Test
    void refreshToken_shouldThrowExceptionWhenCookieMissing() {
        assertThatThrownBy(() -> controller.refreshToken(null))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessage("Refresh Token is missing in cookies");

        assertThatThrownBy(() -> controller.refreshToken("  "))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessage("Refresh Token is missing in cookies");
    }

    @Test
    void refreshToken_viaHttp_withValidCookie_shouldReturnNewAccessToken() throws Exception {
        AuthResultDto authResult = new AuthResultDto("new-access", "new-refresh", 42L, "john@wishoria.app");
        when(authenticationService.refreshToken("old-refresh-token")).thenReturn(authResult);

        mockMvc.perform(post("/api/v1/refresh-token")
                        .cookie(new Cookie("refreshToken", "old-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=new-refresh")));
    }

    @Test
    void refreshToken_viaHttp_withMissingCookie_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/refresh-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void logoutUser_shouldLogoutAndClearCookie_whenCookiePresent() {
        String refreshTokenCookie = "refresh-token-to-invalidate";

        ResponseEntity<ApiResponseWrapper> response = controller.logoutUser(refreshTokenCookie);

        verify(authenticationService).logout(refreshTokenCookie);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        ApiResponseWrapper body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.message()).isEqualTo("Log out successful!");

        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader).isNotNull();
        assertThat(setCookieHeader).contains("refreshToken=");
        assertThat(setCookieHeader).contains("Max-Age=0");
    }

    @Test
    void logoutUser_shouldHandleNullCookieGracefully() {
        ResponseEntity<ApiResponseWrapper> response = controller.logoutUser(null);

        verify(authenticationService).logout(null);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Log out successful!");
    }

    @Test
    void logoutUser_viaHttp_withValidCookie_shouldLogoutAndClearCookie() throws Exception {
        mockMvc.perform(post("/api/v1/sign-out")
                        .cookie(new Cookie("refreshToken", "some-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Log out successful!"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));

        verify(authenticationService).logout("some-token");
    }

    @Test
    void logoutUser_viaHttp_withoutCookie_shouldStillReturn200() throws Exception {
        mockMvc.perform(post("/api/v1/sign-out"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Log out successful!"));

        verify(authenticationService).logout(null);
    }
}
