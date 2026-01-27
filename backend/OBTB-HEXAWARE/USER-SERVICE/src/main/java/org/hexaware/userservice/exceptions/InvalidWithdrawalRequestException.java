package org.hexaware.userservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidWithdrawalRequestException extends RuntimeException {
    public InvalidWithdrawalRequestException(String message) {
        super(message);
    }
}
