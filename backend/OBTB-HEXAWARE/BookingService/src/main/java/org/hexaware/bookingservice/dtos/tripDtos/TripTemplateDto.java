package org.hexaware.bookingservice.dtos.tripDtos;

import org.hexaware.bookingservice.enums.DayOfWeek;
import org.hexaware.bookingservice.enums.TripStatus;
import org.hexaware.bookingservice.enums.TripType;

import java.util.UUID;

public record TripTemplateDto(
        UUID templateId,
        UUID routeId,
        String routeName,
        UUID busId,
        String busName,
        UUID companyId,
        String companyName,
        Double baseFare,
        TripType tripType,
        DayOfWeek scheduledDay,
        java.time.LocalTime regularTime,
        java.time.LocalTime departureTime,
        java.time.LocalTime arrivalTime,
        java.time.LocalDate departureDate,
        java.time.LocalDate arrivalDate,
        boolean isActive
) { }
