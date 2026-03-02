package org.hexaware.busservice.dtos.routesDtos;

public record RouteStopDTO(
        String stopName,
        Integer stopOrder,
        Double distanceFromPreviousStop,
        Integer timeOffsetFromOrigin
) {}

