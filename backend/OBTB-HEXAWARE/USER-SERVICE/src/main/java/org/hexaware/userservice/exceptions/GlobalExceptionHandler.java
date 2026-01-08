package org.hexaware.userservice.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


import javax.management.relation.RoleNotFoundException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<Map<String, Object>> handleUserExsist(UserAlreadyExistException ex, HttpServletRequest request) {
        return buildException(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleRoleNotFound(RoleNotFoundException ex, HttpServletRequest req) {
        return buildException(
                HttpStatus.BAD_REQUEST,"INVALID_ROLE_NAME",ex.getMessage(),req.getRequestURI());
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<Map<String,Object>>  handleInvalidInput(InvalidInputException ex, HttpServletRequest request) {
        return buildException(HttpStatus.BAD_REQUEST, "INVALID_INPUT", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(PasswordPolicyVoilationException.class)
    public ResponseEntity<Map<String, Object>> handlePasswordVoilation(PasswordPolicyVoilationException ex, HttpServletRequest request) {
        return buildException(HttpStatus.BAD_REQUEST, "PASSWORD_VOILED", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UnauthorisedProfileChangeException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedProfileChange(UnauthorisedProfileChangeException ex, HttpServletRequest request) {
        return buildException(HttpStatus.FORBIDDEN, "UNAUTHORIZED", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        return buildException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", ex.getMessage(), request.getRequestURI());
    }

    public ResponseEntity<Map<String, Object>> buildException(HttpStatus status,
                                                              String errorCode,
                                                              String errorMessage,
                                                              String path) {
        Map<String, Object> map = Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error-code", errorCode,
                "error-message", errorMessage,
                "path", path
        );

        return new ResponseEntity<>(map, status);
    }
}
