package org.hexaware.oauthservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SecurityQuestionsMismatchException.class)
    public ResponseEntity<Object> handleSecurityQuestionsMismatch(
            SecurityQuestionsMismatchException ex, WebRequest request) {

        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), "SECURITY_VERIFICATION_FAILED");
    }

    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<Object> handleExpiredToken(ExpiredTokenException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "TOKEN_EXPIRED");
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Object> handleInvalidToken(InvalidTokenException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), "INVALID_TOKEN");
    }

    @ExceptionHandler(UserDoesNotExistException.class)
    public ResponseEntity<Object> handleUserNotFound(UserDoesNotExistException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "USER_NOT_FOUND");
    }

    // Helper method to keep the response structure consistent for Angular
    private ResponseEntity<Object> buildResponse(HttpStatus status, String message, String errorCode) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("errorCode", errorCode); // Custom code for Angular logic
        body.put("message", message);

        return new ResponseEntity<>(body, status);
    }
}