package com.api.wishoria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyActivatedException extends WishoriaException {
    public UserAlreadyActivatedException(String message) {
        super(message);
    }
}
