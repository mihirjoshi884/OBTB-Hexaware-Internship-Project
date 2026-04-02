package org.hexaware.bookingservice.dtos.bookingDtos;

import java.time.LocalDateTime;

public record StopDetailsDto(
        String source,
        LocalDateTime sourceArrival,
        LocalDateTime sourceDeparture,
        String destination,
        LocalDateTime destinationArrival,
        LocalDateTime destinationDeparture
) {}
