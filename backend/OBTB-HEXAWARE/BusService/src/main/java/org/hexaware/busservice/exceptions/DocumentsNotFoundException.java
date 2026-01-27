package org.hexaware.busservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DocumentsNotFoundException extends RuntimeException {
    public DocumentsNotFoundException(String message) {
        super(message);
    }
}
