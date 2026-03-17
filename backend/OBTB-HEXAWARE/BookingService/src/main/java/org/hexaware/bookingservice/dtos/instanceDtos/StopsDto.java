package org.hexaware.bookingservice.dtos.instanceDtos;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record StopsDto(
        UUID stopId,
        String stopName,
        Integer stopOrder,
        LocalDateTime arrivalTime,
        LocalDateTime departureTime
) { }
