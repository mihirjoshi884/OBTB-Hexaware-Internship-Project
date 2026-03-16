package org.hexaware.bookingservice.dtos.routeDtos;

import java.util.List;
import java.util.UUID;

public record RouteResponse(
        UUID routeId,
        String routeName,
        String origin,
        String destination,
        Double totalDistance,
        Integer estimatedDuration,
        List<RouteStopDTO> stops
) {}