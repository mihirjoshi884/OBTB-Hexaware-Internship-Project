package org.hexaware.bookingservice.dtos.eventDtos;

import org.hexaware.bookingservice.enums.EventType;

import java.time.LocalDateTime;

public record SagaEvent<T>(
        EventType eventType,
        T data,
        LocalDateTime dateTime
) { }
