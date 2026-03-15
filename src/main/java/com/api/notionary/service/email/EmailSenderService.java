package com.api.notionary.service.email;

import com.api.notionary.entity.User;

public interface EmailSenderService {
    void send(String to, String emailHtml, String subject);

    void sendConfirmationEmail(String to, String name, String link);

    void sendWishListSharedEmail(User wlOwner, String emailTo, boolean isTargetRegistered, String wlTitle, String wishlistLink, String registrationLink);
}
