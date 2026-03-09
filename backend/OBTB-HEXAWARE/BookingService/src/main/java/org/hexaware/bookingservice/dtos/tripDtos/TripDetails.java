package org.hexaware.bookingservice.dtos.tripDtos;

import org.hexaware.bookingservice.dtos.busDtos.BusFleetResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record TripDetails(
    UUID tripId,
    UUID busId,
    LocalDateTime arrivalTime,
    LocalDateTime departureTime,
    Double baseFare,
    BusFleetResponse busDetails
) { }
