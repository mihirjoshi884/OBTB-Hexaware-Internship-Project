package org.hexaware.busservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusTypeMismatchException extends RuntimeException {
    public BusTypeMismatchException(String message) {
        super(message);
    }
}
