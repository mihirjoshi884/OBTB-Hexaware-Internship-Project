package org.hexaware.transactionservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnableToFetchFundsException.class)
    public ResponseEntity<Map<String,Object>> handleUnableToFetchFundsExceptions(UnableToFetchFundsException ex, HttpServletRequest request){
        return buildException(HttpStatus.NOT_FOUND,
                "error: funds cannot be fetched",
                ex.getMessage(),
                request.getRequestURI());
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
