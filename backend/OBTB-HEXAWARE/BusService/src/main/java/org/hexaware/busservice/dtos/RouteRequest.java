package org.hexaware.busservice.dtos.routesDtos;

import java.util.List;
import java.util.UUID;

public record RouteRequest(
        UUID companyId,
        String routeName,
        String origin,
        String destination,
        Double totalDistance,
        Integer estimatedDuration,
        List<RouteStopDTO> stops
) {}
