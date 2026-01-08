package org.hexaware.userservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistException extends UserServiceBaseException {
    public UserAlreadyExistException(String message) {
        super(message);
    }
}
