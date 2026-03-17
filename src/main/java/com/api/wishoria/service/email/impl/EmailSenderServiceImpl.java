package com.api.wishoria.service.email.impl;

import com.api.wishoria.entity.User;
import com.api.wishoria.service.email.EmailSenderService;
import com.api.wishoria.service.email.EmailValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderServiceImpl implements EmailSenderService {

    @Value("${app.brevo.api-key}")
    private String brevoApiKey;

    @Value("${app.brevo.api-url}")
    private String brevoApiUrl;

    @Value("${app.email.from}")
    private String fromEmail;

    private final TemplateEngine templateEngine;
    private final EmailValidator emailValidator;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    @Override
    public void send(String to, String emailHtml, String subject) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", brevoApiKey);
        headers.set("Content-Type", "application/json");

        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("name", "Wishoria", "email", fromEmail));
        body.put("to", List.of(Map.of("email", to)));
        body.put("subject", subject);
        body.put("htmlContent", emailHtml);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(brevoApiUrl, request, String.class);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
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

        send(normalizedEmail, htmlContent, "Confirm your email address.");
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

        send(normalizedEmail, htmlContent, "Someone shared a Wishlist with you.");
    }
}
