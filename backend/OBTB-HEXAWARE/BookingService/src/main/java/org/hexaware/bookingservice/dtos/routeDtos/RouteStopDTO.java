package org.hexaware.bookingservice.dtos.routeDtos;

public record RouteStopDTO(
        String stopName,
        Integer stopOrder,
        Double distanceFromOrigin,
        Integer timeOffsetFromOrigin
) {}

