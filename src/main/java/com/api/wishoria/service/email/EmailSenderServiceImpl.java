package com.api.wishoria.service.email;

import com.api.wishoria.dto.rabbitMq.EmailPayloadDto;
import com.api.wishoria.entity.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.api.wishoria.util.Constants.EMAIL_EXCHANGE;
import static com.api.wishoria.util.Constants.EMAIL_ROUTING_KEY;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderServiceImpl implements EmailSenderService {

    @Value("${app.url.frontend}")
    private String frontendUrl;

    private final TemplateEngine templateEngine;
    private final EmailValidator emailValidator;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void send(String to, String emailHtml, String subject) {
        EmailPayloadDto payload = new @Valid EmailPayloadDto(to, subject, emailHtml);
        try {
            rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, EMAIL_ROUTING_KEY, payload);
            log.info("Task to send email to {} was successfully pushed to RabbitMQ", to);
        } catch (Exception e) {
            log.error("Failed to enqueue email for {}", to, e);
        }
    }

    @Override
    public void sendConfirmationEmail(String emailTo, String name, String link) {
        String normalizedEmail = emailTo.toLowerCase().trim();
        validateEmail(normalizedEmail);
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("link", link);
        String htmlContent = templateEngine.process("email-confirmation", context);

        send(normalizedEmail, htmlContent, "Confirm your email address.");
    }

    @Override
    public void sendWishListSharedEmail(User wlOwner, String emailTo, boolean isTargetRegistered, String wlTitle,
                                        String wishlistLink, String registrationLink) {
        String normalizedEmail = emailTo.toLowerCase().trim();
        validateEmail(normalizedEmail);
        Context context = new Context();
        context.setVariable("ownerName", wlOwner.getFirstName() + wlOwner.getLastName());
        context.setVariable("wishlistTitle", wlTitle);
        context.setVariable("isTargetRegistered", isTargetRegistered);
        context.setVariable("wishlistLink", wishlistLink);
        context.setVariable("registerLink", registrationLink);
        String htmlContent = templateEngine.process("wishlist-shared-template", context);

        send(normalizedEmail, htmlContent, "Someone shared a Wishlist with you.");
    }

    @Override
    public void sendPasswordRecovery(String emailTo, String name, String token) {
        String normalizedEmail = emailTo.toLowerCase().trim();
        validateEmail(normalizedEmail);

        String resetLink = frontendUrl + "/reset-password?token=" + token;

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("link", resetLink);

        String htmlContent = templateEngine.process("password-reset-template", context);

        send(normalizedEmail, htmlContent, "Reset your Wishoria password");
    }

    private void validateEmail(String normalizedEmail) {
        if (!emailValidator.test(normalizedEmail)) {
            throw new IllegalStateException(String.format("Email %s is invalid.", normalizedEmail));
        }
    }
}
