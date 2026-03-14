package com.api.notionary.service.email;

import com.api.notionary.entity.User;
import com.api.notionary.entity.UserRole;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceServiceImplTest {

    private static final String FROM_EMAIL = "noreply@notionary.app";

    @Mock
    private JavaMailSender javaMailSender;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private EmailValidator emailValidator;

    @InjectMocks
    private com.api.notionary.service.email.impl.EmailServiceServiceImpl emailService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "emailFromValue", FROM_EMAIL);
        user = new User("John", "Doe", "john@example.com", "pass",
                LocalDateTime.now(), UserRole.ROLE_USER);
        user.setId(1L);
    }

    @Test
    void send_shouldSetContentAndSend() throws MessagingException {
        MimeMessage mimeMessage = org.mockito.Mockito.mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.send("to@example.com", "<p>Hello</p>");

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void sendConfirmationEmail_shouldProcessTemplateAndSend() throws MessagingException {
        when(emailValidator.test("user@example.com")).thenReturn(true);
        when(templateEngine.process(eq("email-confirmation"), any(IContext.class))).thenReturn("<html>Confirm</html>");
        MimeMessage mimeMessage = org.mockito.Mockito.mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendConfirmationEmail("user@example.com", "John", "https://link/confirm");

        verify(emailValidator).test("user@example.com");
        verify(templateEngine).process(eq("email-confirmation"), any(IContext.class));
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void sendConfirmationEmail_shouldNormalizeEmail() {
        when(emailValidator.test("user@example.com")).thenReturn(true);
        when(templateEngine.process(eq("email-confirmation"), any(IContext.class))).thenReturn("<html/>");
        when(javaMailSender.createMimeMessage()).thenReturn(org.mockito.Mockito.mock(MimeMessage.class));

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
    void sendWishListSharedEmail_shouldProcessTemplateAndSend() throws MessagingException {
        when(emailValidator.test("friend@example.com")).thenReturn(true);
        when(templateEngine.process(eq("wishlist-shared-template"), any(IContext.class))).thenReturn("<html>Shared</html>");
        MimeMessage mimeMessage = org.mockito.Mockito.mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendWishListSharedEmail(user, "friend@example.com", true, "My List",
                "https://wl.link", "https://register.link");

        verify(emailValidator).test("friend@example.com");
        verify(templateEngine).process(eq("wishlist-shared-template"), any(IContext.class));
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void sendWishListSharedEmail_shouldThrow_whenEmailInvalid() {
        when(emailValidator.test("bad")).thenReturn(false);

        assertThatThrownBy(() -> emailService.sendWishListSharedEmail(user, "bad", false, "List", "u", "r"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("invalid");
    }
}
