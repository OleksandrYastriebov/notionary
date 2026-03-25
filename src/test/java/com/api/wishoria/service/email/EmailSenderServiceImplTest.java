package com.api.wishoria.service.email;

import com.api.wishoria.dto.rabbitMq.EmailPayloadDto;
import com.api.wishoria.entity.User;
import com.api.wishoria.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;

import java.time.Instant;

import static com.api.wishoria.util.Constants.EMAIL_EXCHANGE;
import static com.api.wishoria.util.Constants.EMAIL_ROUTING_KEY;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailSenderServiceImplTest {

    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private EmailValidator emailValidator;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EmailSenderServiceImpl emailService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("John", "Doe", "john@example.com", "pass",
                Instant.now(), UserRole.ROLE_USER);
        user.setId(1L);
    }

    @Test
    void send_shouldBuildRequestAndPost() {
        emailService.send("to@example.com", "<p>Hello</p>", "test subject");

        verify(rabbitTemplate).convertAndSend(eq(EMAIL_EXCHANGE), eq(EMAIL_ROUTING_KEY), any(EmailPayloadDto.class));
    }

    @Test
    void sendConfirmationEmail_shouldProcessTemplateAndSend() {
        when(emailValidator.test("user@example.com")).thenReturn(true);
        when(templateEngine.process(eq("email-confirmation"), any(IContext.class))).thenReturn("<html>Confirm</html>");

        emailService.sendConfirmationEmail("user@example.com", "John", "https://link/confirm");

        verify(emailValidator).test("user@example.com");
        verify(templateEngine).process(eq("email-confirmation"), any(IContext.class));
        verify(rabbitTemplate).convertAndSend(eq(EMAIL_EXCHANGE), eq(EMAIL_ROUTING_KEY), any(EmailPayloadDto.class));
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
        verify(rabbitTemplate).convertAndSend(eq(EMAIL_EXCHANGE), eq(EMAIL_ROUTING_KEY), any(EmailPayloadDto.class));
    }

    @Test
    void sendWishListSharedEmail_shouldThrow_whenEmailInvalid() {
        when(emailValidator.test("bad")).thenReturn(false);

        assertThatThrownBy(() -> emailService.sendWishListSharedEmail(user, "bad", false, "List", "u", "r"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("invalid");
    }
}
