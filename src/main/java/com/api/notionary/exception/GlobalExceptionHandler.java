package com.api.notionary.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String TIMESTAMP = "timestamp";
    private static final String STATUS = "status";
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";
    private static final String PATH = "path";
    private static final String DETAILS = "details";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put(TIMESTAMP, LocalDateTime.now());
        errorDetails.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorDetails.put(ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        errorDetails.put(MESSAGE, ex.getMessage());
        errorDetails.put(PATH, request.getDescription(false).replace("uri=", ""));
        errorDetails.put(DETAILS, getStackTraceAsString(ex));

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put(TIMESTAMP, LocalDateTime.now());
        errorDetails.put(STATUS, HttpStatus.BAD_REQUEST.value());
        errorDetails.put(ERROR, HttpStatus.BAD_REQUEST.getReasonPhrase());
        errorDetails.put(MESSAGE, ex.getMessage());
        errorDetails.put(PATH, request.getDescription(false).replace("uri=", ""));
        errorDetails.put(DETAILS, getStackTraceAsString(ex));

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put(STATUS, HttpStatus.NOT_FOUND.value());
        errorDetails.put(MESSAGE, ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WishlistNotFoundException.class)
    public ResponseEntity<Object> handleWishlistNotFoundException(WishlistNotFoundException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put(STATUS, HttpStatus.NOT_FOUND.value());
        errorDetails.put(MESSAGE, ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WishlistItemNotFoundException.class)
    public ResponseEntity<Object> handleWishlistItemNotFoundException(WishlistItemNotFoundException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put(STATUS, HttpStatus.NOT_FOUND.value());
        errorDetails.put(MESSAGE, ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid or missing request body: " + ex.getLocalizedMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Validation failed: " + ex.getFieldError().getDefaultMessage());
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<?> handleInvalidFormat(InvalidFormatException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid request body format: " + ex.getMessage());
    }

    private String getStackTraceAsString(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
