package org.hexaware.userservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ResponseDto<T> {
    private T body;
    private int status;
    private String message;
}
