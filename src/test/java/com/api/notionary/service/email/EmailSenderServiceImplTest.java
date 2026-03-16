package com.api.notionary.service.email;

import com.api.notionary.entity.User;
import com.api.notionary.entity.UserRole;
import com.api.notionary.service.email.impl.EmailSenderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailSenderServiceImplTest {

    private static final String FROM_EMAIL = "noreply@notionary.app";
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final String BREVO_API_KEY = "test-api-key";

    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private EmailValidator emailValidator;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmailSenderServiceImpl emailService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", FROM_EMAIL);
        ReflectionTestUtils.setField(emailService, "brevoApiUrl", BREVO_API_URL);
        ReflectionTestUtils.setField(emailService, "brevoApiKey", BREVO_API_KEY);
        ReflectionTestUtils.setField(emailService, "restTemplate", restTemplate);
        user = new User("John", "Doe", "john@example.com", "pass",
                Instant.now(), UserRole.ROLE_USER);
        user.setId(1L);
    }

    @Test
    void send_shouldBuildRequestAndPost() {
        emailService.send("to@example.com", "<p>Hello</p>", "test subject");

        verify(restTemplate).postForEntity(eq(BREVO_API_URL), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void sendConfirmationEmail_shouldProcessTemplateAndSend() {
        when(emailValidator.test("user@example.com")).thenReturn(true);
        when(templateEngine.process(eq("email-confirmation"), any(IContext.class))).thenReturn("<html>Confirm</html>");

        emailService.sendConfirmationEmail("user@example.com", "John", "https://link/confirm");

        verify(emailValidator).test("user@example.com");
        verify(templateEngine).process(eq("email-confirmation"), any(IContext.class));
        verify(restTemplate).postForEntity(eq(BREVO_API_URL), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void sendConfirmationEmail_shouldNormalizeEmail() {
        when(emailValidator.test("user@example.com")).thenReturn(true);
        when(templateEngine.process(eq("email-confirmation"), any(IContext.class))).thenReturn("<html/>");

        emailService.sendConfirmationEmail("  USER@Example.COM  ", "John", "https://link");

        verify(emailValidator).test("user@example.com");
    }

    @Test
    void sendConfirmationEmail_shouldThrow_whenEmailInvalid() {
        when(emailValidator.test("invalid")).thenReturn(false);

        assertThatThrownBy(() -> emailService.sendConfirmationEmail("invalid", "Name", "https://link"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("invalid");
        verify(templateEngine, never()).process(any(String.class), any(IContext.class));
    }

    @Test
    void sendWishListSharedEmail_shouldProcessTemplateAndSend() {
        when(emailValidator.test("friend@example.com")).thenReturn(true);
        when(templateEngine.process(eq("wishlist-shared-template"), any(IContext.class))).thenReturn("<html>Shared</html>");

        emailService.sendWishListSharedEmail(user, "friend@example.com", true, "My List",
                "https://wl.link", "https://register.link");

        verify(emailValidator).test("friend@example.com");
        verify(templateEngine).process(eq("wishlist-shared-template"), any(IContext.class));
        verify(restTemplate).postForEntity(eq(BREVO_API_URL), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void sendWishListSharedEmail_shouldThrow_whenEmailInvalid() {
        when(emailValidator.test("bad")).thenReturn(false);

        assertThatThrownBy(() -> emailService.sendWishListSharedEmail(user, "bad", false, "List", "u", "r"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("invalid");
    }
}
