package org.hexaware.bookingservice.dtos.instanceDtos;

import org.hexaware.bookingservice.enums.SeatStatus;
import org.hexaware.bookingservice.enums.SeatType;

import java.util.UUID;

public record EnrichedSeatDto(
        String seatNumber,
        int x_coordinate,
        int y_coordinate,
        int deck,
        SeatType type, // SEATER, SLEEPER, WALKWAY
        boolean isWindow,

        // Functional/Dynamic Data (from TripSeat table)
        UUID tripSeatId,  // NULL if it's a WALKWAY
        SeatStatus status    // AVAILABLE, BOOKED, BLOCKED
) { }
