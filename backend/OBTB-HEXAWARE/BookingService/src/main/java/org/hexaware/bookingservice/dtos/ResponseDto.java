package org.hexaware.bookingservice.dtos;

import lombok.*;

@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
public class ResponseDto<T> {
    private T body;
    private int status;
    private String message;
}
