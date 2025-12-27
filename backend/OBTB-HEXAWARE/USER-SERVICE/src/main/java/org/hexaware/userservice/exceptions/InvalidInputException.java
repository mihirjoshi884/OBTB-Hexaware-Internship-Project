package org.hexaware.userservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidInputException extends UserServiceBaseException {

    public InvalidInputException(String message) {
        super(message);
    }
}
