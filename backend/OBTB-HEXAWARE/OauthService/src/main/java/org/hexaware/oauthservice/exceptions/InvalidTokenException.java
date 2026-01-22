package org.hexaware.oauthservice.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidTokenException extends AuthServiceBaseException{
    public InvalidTokenException(String message) {
        super(message);
    }
}
