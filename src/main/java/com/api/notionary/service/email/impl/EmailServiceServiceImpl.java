package com.api.notionary.service.email.impl;

import com.api.notionary.entity.User;
import com.api.notionary.service.email.EmailSenderService;
import com.api.notionary.service.email.EmailValidator;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceServiceImpl implements EmailSenderService {

    @Value("${email.from.value}")
    private String emailFromValue;

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final EmailValidator emailValidator;

    @Override
    public void send(String to, String emailText) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(emailText, true);
            helper.setTo(to);
            helper.setSubject("Confirm your email address.");
            helper.setFrom(emailFromValue);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException ex) {
            log.error("Failed to send email.", ex);
        }
    }

    @Override
    public void sendConfirmationEmail(String emailTo, String name, String link) {
        String normalizedEmail = emailTo.toLowerCase().trim();
        if (!emailValidator.test(normalizedEmail)) {
            throw new IllegalStateException(String.format("Email %s is invalid.", normalizedEmail));
        }
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("link", link);
        String htmlContent = templateEngine.process("email-confirmation", context);

        send(normalizedEmail, htmlContent);
    }

    @Override
    public void sendWishListSharedEmail(User wlOwner, String emailTo, boolean isTargetRegistered, String wlTitle,
                                        String wishlistLink, String registrationLink) {
        String normalizedEmail = emailTo.toLowerCase().trim();
        if (!emailValidator.test(normalizedEmail)) {
            throw new IllegalStateException(String.format("Email %s is invalid.", normalizedEmail));
        }
        Context context = new Context();
        context.setVariable("ownerName", wlOwner.getFirstName() + wlOwner.getLastName());
        context.setVariable("wishlistTitle", wlTitle);
        context.setVariable("isTargetRegistered", isTargetRegistered);
        context.setVariable("wishlistLink", wishlistLink);
        context.setVariable("registerLink", registrationLink);
        String htmlContent = templateEngine.process("wishlist-shared-template", context);

        send(normalizedEmail, htmlContent);
    }
}
