package org.hexaware.notificationservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class NotificationEvent<T> {
    private T data; // This can now be Summary, String, Map, etc.
    private String recipientIdentifier;
    private long timestamp;
}
