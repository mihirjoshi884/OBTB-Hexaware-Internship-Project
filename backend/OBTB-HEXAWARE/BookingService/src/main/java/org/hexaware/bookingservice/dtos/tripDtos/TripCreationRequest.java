package org.hexaware.bookingservice.dtos.tripDtos;

import org.hexaware.bookingservice.enums.TripType;

import java.time.LocalDateTime;
import java.util.UUID;

public record TripCreationRequest(
        UUID routeId,
        UUID busId,
        UUID companyId,
        TripType tripType,

        // Matches selectedDepartureDate (YYYY-MM-DD)
        String departureDate,
        // Matches selectedDepartureTime (HH:mm)
        String departureTime,

        // Matches selectedArrivalDate (YYYY-MM-DD)
        String arrivalDate,
        // Matches selectedArrivalTime (HH:mm)
        String arrivalTime,

        // Matches selectedDay ("MONDAY", etc.)
        String scheduledDay,

        // Matches selectedRegularDepartureTime (HH:mm)
        String regularDepartureTime,

        Double baseFare
) { }
