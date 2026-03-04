package org.hexaware.busservice.dtos.routeDtos;

public record RouteStopDTO(
        String stopName,
        Integer stopOrder,
        Double distanceFromPreviousStop,
        Integer timeOffsetFromOrigin
) {}

