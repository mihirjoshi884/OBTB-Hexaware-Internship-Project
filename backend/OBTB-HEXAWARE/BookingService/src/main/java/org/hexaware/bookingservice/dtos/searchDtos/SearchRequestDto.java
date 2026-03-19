package org.hexaware.bookingservice.dtos.searchDtos;

public record SearchRequestDto(
    String source,
    String destination,
    String departureDate,
    String returnDate
) { }
