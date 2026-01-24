package org.hexaware.transactionservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ResponseDto<T> {
    private T body;
    private int status;
    private String message;
}
