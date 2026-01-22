package org.hexaware.oauthservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class NotificationEvent<T> {
    private T data; // This can now be Summary, String, Map, etc.
    private String recipientIdentifier;
    private long timestamp;
}
