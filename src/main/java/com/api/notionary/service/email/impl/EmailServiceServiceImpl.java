package com.api.notionary.service.email.impl;

import com.api.notionary.service.email.EmailSenderService;
import com.api.notionary.service.email.EmailValidator;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
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
    @Async
    public void send(String to, String email) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(email, true);
            helper.setTo(to);
            helper.setSubject("Confirm your email address.");
            helper.setFrom(emailFromValue);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException ex) {
            log.error("Failed to send email.", ex);
        }
    }

    @Override
    @Async
    public void sendConfirmationEmail(String emailTo, String name, String link) {
        if (!emailValidator.test(emailTo)) {
            throw new IllegalStateException(String.format("Email %s is invalid.", emailTo));
        }
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("link", link);
        String htmlContent = templateEngine.process("email-confirmation", context);

        send(emailTo, htmlContent);
    }
}
