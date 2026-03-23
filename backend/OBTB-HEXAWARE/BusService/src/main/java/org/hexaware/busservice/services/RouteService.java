package org.hexaware.busservice.services;

import org.hexaware.busservice.dtos.ResponseDto;
import org.hexaware.busservice.dtos.routeDtos.FetchRoute;
import org.hexaware.busservice.dtos.routeDtos.RouteRequest;
import org.hexaware.busservice.dtos.routeDtos.RouteResponse;

import java.util.List;
import java.util.UUID;

public interface RouteService {

    public ResponseDto<RouteResponse> createRoute(RouteRequest request);
    public ResponseDto<List<RouteResponse>> getCompanyRoutes(UUID companyId) ;
    public ResponseDto<RouteResponse> updateRoute(UUID routeId, RouteRequest request);
    public ResponseDto deleteRoute(UUID routeId);
    public ResponseDto<List<RouteResponse>> fetchRouteBetweenSourceAndDestination(FetchRoute route);
}
