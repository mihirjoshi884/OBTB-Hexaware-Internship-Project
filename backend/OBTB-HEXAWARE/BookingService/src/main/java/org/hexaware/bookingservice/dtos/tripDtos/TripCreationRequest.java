package org.hexaware.bookingservice.dtos.tripDtos;

import org.hexaware.bookingservice.enums.TripType;

import java.time.LocalDateTime;
import java.util.UUID;

public record TripCreationRequest(
        UUID routeId,
        UUID busId,
        UUID companyId,
        TripType tripType,

        // The first time this bus ever runs
        LocalDateTime firstDepartureTime,
        LocalDateTime firstArrivalTime,

        // Explicit schedule data for the Template
        // (You can extract these from the dates above in the Service,
        // but having them here is clearer for the UI/API)
        java.time.DayOfWeek scheduledDay,
        java.time.LocalTime dailyDepartureTime,

        Double baseFare
) { }
