package com.api.notionary.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Slf4j
public class WishlistNotFoundException extends RuntimeException{
    public WishlistNotFoundException(String message) {
        super(message);
    }
}
