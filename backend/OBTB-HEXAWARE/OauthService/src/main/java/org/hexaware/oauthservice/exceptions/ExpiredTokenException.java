package org.hexaware.oauthservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ExpiredTokenException extends AuthServiceBaseException{

    public ExpiredTokenException(String message) {
        super(message);
    }
}
