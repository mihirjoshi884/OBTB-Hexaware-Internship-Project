package org.hexaware.busservice.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hexaware.busservice.dtos.ResponseDto;
import org.hexaware.busservice.dtos.routeDtos.FetchRoute;
import org.hexaware.busservice.dtos.routeDtos.RouteRequest;
import org.hexaware.busservice.dtos.routeDtos.RouteResponse;
import org.hexaware.busservice.dtos.routeDtos.RouteStopDTO;
import org.hexaware.busservice.entities.Company;
import org.hexaware.busservice.entities.Route;
import org.hexaware.busservice.entities.RouteStop;
import org.hexaware.busservice.repositories.CompanyRepository;
import org.hexaware.busservice.repositories.RouteRepository;
import org.hexaware.busservice.services.RouteService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Automatically injects final fields like repositories
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public ResponseDto<RouteResponse> createRoute(RouteRequest request) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + request.companyId()));

        Route route = new Route();
        route.setCompany(company);
        updateRouteFields(route, request);

        // Map and link stops
        if (request.stops() != null) {
            List<RouteStop> stopEntities = request.stops().stream()
                    .map(dto -> mapToStopEntity(dto, route))
                    .collect(Collectors.toList());
            route.getStops().addAll(stopEntities);
        }

        Route savedRoute = routeRepository.save(route);
        return new ResponseDto<>(mapToResponse(savedRoute), 200, "Route created successfully");
    }

    @Override
    public ResponseDto<List<RouteResponse>> getCompanyRoutes(UUID companyId) {
        List<RouteResponse> routes = routeRepository.findAllByCompanyCompanyId(companyId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return new ResponseDto<>(routes, 200, "Company routes retrieved successfully");
    }

    @Override
    @Transactional
    public ResponseDto<RouteResponse> updateRoute(UUID routeId, RouteRequest request) {
        Route existingRoute = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found with ID: " + routeId));

        updateRouteFields(existingRoute, request);

        // Clear and replace stops (leverages orphanRemoval = true)
        existingRoute.getStops().clear();
        if (request.stops() != null) {
            List<RouteStop> newStops = request.stops().stream()
                    .map(dto -> mapToStopEntity(dto, existingRoute))
                    .collect(Collectors.toList());
            existingRoute.getStops().addAll(newStops);
        }

        Route updatedRoute = routeRepository.save(existingRoute);
        return new ResponseDto<>(mapToResponse(updatedRoute), 200, "Route updated successfully");
    }

    @Override
    @Transactional
    public ResponseDto<RouteResponse> deleteRoute(UUID routeId) {
        Route existingRoute = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found with ID: " + routeId));

        // Map to response before deletion to return the deleted data context
        RouteResponse deletedRouteResponse = mapToResponse(existingRoute);
        routeRepository.delete(existingRoute);

        return new ResponseDto<>(deletedRouteResponse, 200, "Route deleted successfully");
    }

    @Override
    public ResponseDto<List<RouteResponse>> fetchRouteBetweenSourceAndDestination(FetchRoute route) {
        var routes = routeRepository.findRoutesByOriginAndDestination(route.source(), route.destination());
        var response = routes.stream().map(this::mapToResponse).collect(Collectors.toList());
        String message = response.isEmpty() ? "No routes available for this path." : "Routes found successfully!";
        System.out.println(response);
        return new ResponseDto<>(response, 200, message);
    }

    // --- Helper Methods for Cleaner Code ---

    private void updateRouteFields(Route route, RouteRequest request) {
        route.setRouteName(request.routeName());
        route.setOrigin(request.origin());
        route.setDestination(request.destination());
        route.setTotalDistance(request.totalDistance());
        route.setEstimatedDuration(request.estimatedDuration());
    }

    private RouteStop mapToStopEntity(RouteStopDTO dto, Route route) {
        RouteStop stop = new RouteStop();
        stop.setStopName(dto.stopName());
        stop.setStopOrder(dto.stopOrder());
        stop.setDistanceFromOrigin(dto.distanceFromOrigin());
        stop.setTimeOffsetFromOrigin(dto.timeOffsetFromOrigin());
        stop.setRoute(route);
        return stop;
    }

    private RouteResponse mapToResponse(Route route) {
        List<RouteStopDTO> stopDTOs = route.getStops().stream()
                .map(s -> new RouteStopDTO(
                        s.getStopName(),
                        s.getStopOrder(),
                        s.getDistanceFromOrigin(),
                        s.getTimeOffsetFromOrigin()))
                .collect(Collectors.toList());

        return new RouteResponse(
                route.getRouteId(),
                route.getRouteName(),
                route.getOrigin(),
                route.getDestination(),
                route.getTotalDistance(),
                route.getEstimatedDuration(),
                stopDTOs
        );
    }
}