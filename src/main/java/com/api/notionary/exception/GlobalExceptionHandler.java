package com.api.notionary.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.jsonwebtoken.JwtException;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Throwable ex, WebRequest webRequest) {
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
    public ResponseEntity<ErrorResponse> handleEntityNotFoundExceptions(EntityNotFoundException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), "Invalid Email or Password.", request);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(DisabledException ex, WebRequest webRequest) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(),
                "Account is locked. Confirm your email address.", webRequest);
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ErrorResponse> handleTokenRefreshException(TokenRefreshException ex, WebRequest webRequest) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), webRequest);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest webRequest) {
        log.warn("Malformed or missing request body: {}", ex.getMessage());

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Malformed JSON Request",
                "The request body is missing or cannot be parsed. Please check the JSON format.", webRequest);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest webRequest) {
        String errorMessage = ex.getFieldError() != null ? ex.getFieldError().getDefaultMessage() : "Invalid method arguments";
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Error",
                "Validation failed: " + errorMessage, webRequest);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFormat(InvalidFormatException ex, WebRequest webRequest) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Format Error",
                "Invalid request body format: " + ex.getMessage(), webRequest);
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ErrorResponse> handleMessagingException(MessagingException ex, WebRequest webRequest) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Mail send Exception",
                "Error trying to send email: " + ex.getMessage(), webRequest);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest webRequest) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Access denied",
                "You are not authorized to perform this action. " + ex.getMessage(), webRequest);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex, WebRequest webRequest) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(),
                "User already exists in repository.", webRequest);
    }

    @ExceptionHandler(UserAlreadyActivatedException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyActivatedException(UserAlreadyActivatedException ex, WebRequest webRequest) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(),
                "Conflict.", webRequest);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpiredException(TokenExpiredException ex, WebRequest webRequest) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(),
                "Token is expired.", webRequest);
    }

    @ExceptionHandler(TokenInvalidException.class)
    public ResponseEntity<ErrorResponse> handleTokenInvalidException(TokenInvalidException ex, WebRequest webRequest) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(),
                "Token is invalid.", webRequest);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(JwtException ex, WebRequest request) {
        log.warn("JWT Verification failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Token error",
                "Token has expired or is invalid.", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Resource Not Found", "The requested endpoint or resource does not exist. Please check the URL.", request);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Username not found", ex.getMessage(), request);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceededException(RateLimitExceededException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), "Rate limit exceeded", request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex, WebRequest webRequest) {
        String parameterName = ex.getParameterName();
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Missing Parameter",
                String.format("Required request parameter '%s' is not present.", parameterName), webRequest);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex, WebRequest webRequest) {
        return buildErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type",
                String.format("Content type '%s' not supported. Please use 'multipart/form-data'.", ex.getContentType()), webRequest);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String exceptionMessage, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(status.value(), exceptionMessage, getRequestPath(request));
        return new ResponseEntity<>(errorResponse, buildHeaders(), status);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String exceptionMessage,
                                                             String message, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(status.value(), exceptionMessage, message, getRequestPath(request));
        return new ResponseEntity<>(errorResponse, buildHeaders(), status);
    }

    private String getRequestPath(WebRequest request) {
        return request != null ? request.getDescription(false).replace("uri=", "") : "";
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return httpHeaders;
    }
}
