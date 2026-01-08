package org.hexaware.notificationservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class NotificationEvent {
    private Summary data;
    private String recipientIdentifier;
    private long timestamp;
}
