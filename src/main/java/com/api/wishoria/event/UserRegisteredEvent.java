package com.api.wishoria.event;

import com.api.wishoria.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserRegisteredEvent extends ApplicationEvent {

    private final User user;
    private final String confirmationToken;

    public UserRegisteredEvent(Object source, User user, String confirmationToken) {
        super(source);
        this.user = user;
        this.confirmationToken = confirmationToken;
    }
}
