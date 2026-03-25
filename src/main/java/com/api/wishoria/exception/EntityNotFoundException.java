package com.api.wishoria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends ResourceNotFoundException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
