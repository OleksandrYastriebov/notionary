package com.api.wishoria.event;

import com.api.wishoria.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PasswordRecoveryEvent extends ApplicationEvent {

    private final User owner;
    private final String token;

    public PasswordRecoveryEvent(Object source, User owner, String token) {
        super(source);
        this.owner = owner;
        this.token = token;
    }
}
