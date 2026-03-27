package org.hexaware.bookingservice.dtos.searchDtos;

import org.hexaware.bookingservice.enums.TripStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record TripSearchResponseDto(
        UUID instanceId,
        String routeName,
        String busName,
        String source,
        String destination,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        Double fare,
        long availableSeats,
        TripStatus status,
        String direction
) {}