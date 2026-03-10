package com.api.notionary.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest webRequest) {
        log.error("Unexpected error occurred", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Hidden for security reasons",
                "Internal Server Error", webRequest);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex, WebRequest webRequest) {
        log.warn("Unexpected error occurred: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(),
                "Business rule violation", webRequest);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(RuntimeException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), "Invalid Email or Password.", request);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(DisabledException ex, WebRequest webRequest) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(),
                "Account is locked.Confirm your email address.", webRequest);
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ErrorResponse> handleTokenRefreshException(TokenRefreshException ex, WebRequest webRequest) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), webRequest);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest webRequest) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(),
                "Invalid or missing request body: " + ex.getLocalizedMessage(), webRequest);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest webRequest) {
        String errorMessage = ex.getFieldError() != null
                ? ex.getFieldError().getDefaultMessage()
                : "Invalid data";
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(),
                "Validation failed: " + errorMessage, webRequest);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFormat(InvalidFormatException ex, WebRequest webRequest) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(),
                "Invalid request body format: " + ex.getMessage(), webRequest);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String exceptionMessage, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                exceptionMessage,
                request != null ? request.getDescription(false).replace("uri=", "") : ""
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String exceptionMessage,
                                                             String message, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                exceptionMessage,
                message,
                request != null ? request.getDescription(false).replace("uri=", "") : ""
        );
        return new ResponseEntity<>(errorResponse, status);
    }
/*    private HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173");
        httpHeaders.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        return httpHeaders;
    }*/
}
