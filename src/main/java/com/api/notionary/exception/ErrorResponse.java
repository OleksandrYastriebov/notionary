package com.api.notionary.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private int statusCode;
    private String errorMessage;
    private String message;
    private String details;

    public ErrorResponse(int statusCode, String errorMessage, String details) {
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.details = details;
    }
}
