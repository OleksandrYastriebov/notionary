package com.api.notionary.service.email;

public interface EmailSender {
    void send(String to, String email);
}
