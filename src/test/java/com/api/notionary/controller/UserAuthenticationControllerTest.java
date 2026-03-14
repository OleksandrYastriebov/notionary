package com.api.notionary.controller;

import com.api.notionary.dto.ApiResponseWrapper;
import com.api.notionary.dto.payload.request.user.SignInRequest;
import com.api.notionary.dto.payload.request.user.SignUpRequest;
import com.api.notionary.dto.token.AuthResultDto;
import com.api.notionary.dto.token.JwtDto;
import com.api.notionary.dto.token.TokenRefreshDto;
import com.api.notionary.exception.TokenRefreshException;
import com.api.notionary.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private UserAuthenticationController controller;

    private static final int REFRESH_COOKIE_MAX_AGE = 3600;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "refreshCookieMaxAge", REFRESH_COOKIE_MAX_AGE);
    }

    @Test
    void signUp_shouldReturnOkWithServiceResponse() {
        SignUpRequest request = new SignUpRequest(
                "John",
                "Doe",
                "user@notionary.app",
                "password123"
        );
        ApiResponseWrapper serviceResponse = new ApiResponseWrapper("User registered successfully");

        when(authenticationService.signUp(any(SignUpRequest.class))).thenReturn(serviceResponse);

        ResponseEntity<ApiResponseWrapper> response = controller.signUp(request);

        verify(authenticationService).signUp(request);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("User registered successfully");
    }

    @Test
    void signIn_shouldReturnJwtDtoAndSetRefreshCookie() {
        SignInRequest request = new SignInRequest("user@notionary.app", "password123");
        AuthResultDto authResult = new AuthResultDto(
                "access-token-value",
                "refresh-token-value",
                42L,
                "user@notionary.app"
        );

        when(authenticationService.signIn(any(SignInRequest.class))).thenReturn(authResult);

        ResponseEntity<JwtDto> response = controller.signIn(request);

        verify(authenticationService).signIn(request);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        JwtDto body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.jwtToken()).isEqualTo("access-token-value");
        assertThat(body.id()).isEqualTo(42L);
        assertThat(body.email()).isEqualTo("user@notionary.app");

        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader).isNotNull();
        assertThat(setCookieHeader).contains("refreshToken=refresh-token-value");
    }

    @Test
    void refreshToken_shouldExchangeCookieForNewTokens() {
        String refreshTokenCookie = "existing-refresh-token";
        AuthResultDto authResult = new AuthResultDto(
                "new-access-token",
                "new-refresh-token",
                42L,
                "user@notionary.app"
        );

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

        ApiResponseWrapper body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.message()).isEqualTo("Log out successful!");

        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader).isNotNull();
        assertThat(setCookieHeader).contains("refreshToken=");
        assertThat(setCookieHeader).contains("Max-Age=0");
    }
}

