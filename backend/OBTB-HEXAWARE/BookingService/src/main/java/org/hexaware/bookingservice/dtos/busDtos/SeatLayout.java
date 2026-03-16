package org.hexaware.bookingservice.dtos.busDtos;

import org.hexaware.bookingservice.enums.SeatType;

public record SeatLayout(
        String id,
        SeatType type, //(SEATER, SLEEPER, WALKWAY;)
        boolean isWindow,
        int x_coordinate,
        int y_coordinate,
        int deck
) { }
