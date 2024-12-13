package com.api.yastro.notionary.service.email;

public interface EmailSender {
    void send(String to, String email);
}
