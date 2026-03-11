package com.api.notionary.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    @Schema(example = "404", description = "HTTP Status Code")
    private int statusCode;

    @Schema(example = "Not Found", description = "Technical exception message")
    private String errorMessage;

    @Schema(example = "Entity can not be found.", description = "User-friendly message")
    private String message;

    @Schema(example = "/api/entity/10", description = "Request path")
    private String details;

    public ErrorResponse(int statusCode, String errorMessage, String details) {
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.details = details;
    }
}
