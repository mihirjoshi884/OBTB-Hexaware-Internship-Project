package org.hexaware.userservice.dtos.eventDtos;



import org.hexaware.userservice.enums.EventType;

import java.time.LocalDateTime;

public record SagaEvent<T>(
        EventType eventType,
        T data,
        LocalDateTime dateTime
) { }
