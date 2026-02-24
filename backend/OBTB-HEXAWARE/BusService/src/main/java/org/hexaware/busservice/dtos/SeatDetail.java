package org.hexaware.busservice.dtos;

import org.hexaware.busservice.enums.SeatType;

public record SeatDetail(
        String id,
        SeatType type, //(SEATER, SLEEPER, WALKWAY;)
        boolean isWindow,
        int x_coordinate,
        int y_coordinate

) { }
