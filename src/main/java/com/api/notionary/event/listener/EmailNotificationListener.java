package com.api.notionary.event.listener;

import com.api.notionary.event.UserRegisteredEvent;
import com.api.notionary.event.WishlistSharedEvent;
import com.api.notionary.service.email.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationListener {
    private final EmailSenderService emailSenderService;

    @Value("${app.url.backend}")
    private String appUrl;

    @Value("${app.url.frontend}")
    private String frontendUrl;

    /**
     * Listen for the UserRegisteredEvent.
     * Async Guarantees a separate thread for this method.
     */
    @Async
    @EventListener
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Handling UserRegisteredEvent for user: {}", event.getUser().getEmail());
        String activationLink = String.format("%s/api/v1/confirm-email?token=%s", appUrl, event.getConfirmationToken());
        emailSenderService.sendConfirmationEmail(
                event.getUser().getEmail().toLowerCase().trim(),
                event.getUser().getFirstName(),
                activationLink
        );
    }

    /**
     * Listen for the WishlistSharedEvent.
     */
    @Async
    @EventListener
    public void handleWishlistSharedEvent(WishlistSharedEvent event) {
        log.info("Handling WishlistSharedEvent. Sending email to: {}", event.getTargetEmail());

        String wishlistLink = String.format("%s/wishlists/%s", frontendUrl, event.getWishlist().getId());
        String registrationLink = String.format("%s/sign-up", frontendUrl);

        emailSenderService.sendWishListSharedEmail(event.getOwner(),
                event.getTargetEmail(),
                event.isTargetRegistered(),
                event.getWishlist().getTitle(),
                wishlistLink,
                registrationLink);
    }
}
