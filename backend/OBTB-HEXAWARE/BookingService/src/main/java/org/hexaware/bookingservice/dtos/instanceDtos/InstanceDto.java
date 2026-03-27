package org.hexaware.bookingservice.dtos.instanceDtos;

import org.hexaware.bookingservice.enums.TripStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InstanceDto(
        UUID instanceId,
        UUID templateId,
        String source,
        String destination,
        LocalDateTime actualDeparture,
        LocalDateTime actualArrival,
        List<StopsDto> stops,
        TripStatus status
) { }
