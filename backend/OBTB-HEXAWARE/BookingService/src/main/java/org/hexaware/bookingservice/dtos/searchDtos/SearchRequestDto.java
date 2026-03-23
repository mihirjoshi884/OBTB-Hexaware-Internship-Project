package org.hexaware.bookingservice.dtos.searchDtos;

import org.hexaware.bookingservice.enums.JourneyType;

public record SearchRequestDto(
    String source,
    String destination,
    String departureDate,
    String returnDate,
    JourneyType journeyType
) { }
