package com.api.wishoria.service.email;

import com.api.wishoria.entity.User;

public interface EmailSenderService {
    void send(String to, String emailHtml, String subject);

    void sendConfirmationEmail(String to, String name, String link);

    void sendWishListSharedEmail(User wlOwner, String emailTo, boolean isTargetRegistered, String wlTitle, String wishlistLink, String registrationLink);
}
