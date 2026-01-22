package org.hexaware.oauthservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class SecurityQuestionsMismatchException extends AuthServiceBaseException {
    public SecurityQuestionsMismatchException(String message) {
        super(message);
    }
}
