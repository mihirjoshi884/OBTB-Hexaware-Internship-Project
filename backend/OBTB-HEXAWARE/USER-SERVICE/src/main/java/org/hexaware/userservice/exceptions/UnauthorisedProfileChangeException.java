package org.hexaware.userservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorisedProfileChangeException extends UserServiceBaseException {
    public UnauthorisedProfileChangeException(String message) {
        super(message);
    }
}
