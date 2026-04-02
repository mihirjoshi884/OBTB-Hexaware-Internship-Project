package org.hexaware.bookingservice.dtos.bookingDtos;

import java.util.List;
import java.util.UUID;

public record BookingRequestDto(
        UUID tripInstanceId,
        String source,
        String destination,
        UUID userId,
        List<PassengerDetailDto> passengers

) { }
