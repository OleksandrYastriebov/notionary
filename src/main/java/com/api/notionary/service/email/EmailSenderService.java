package com.api.notionary.service.email;

public interface EmailSenderService {
    void send(String to, String email);
    void sendConfirmationEmail(String to, String name, String link);
}
