package org.hexaware.busservice.services;

import org.hexaware.busservice.dtos.ResponseDto;
import org.hexaware.busservice.dtos.routesDtos.RouteRequest;
import org.hexaware.busservice.dtos.routesDtos.RouteResponse;
import org.hexaware.busservice.entities.Route;

import java.util.List;
import java.util.UUID;

public interface RouteService {

    public ResponseDto<RouteResponse> createRoute(RouteRequest request);
    public ResponseDto<List<RouteResponse>> getCompanyRoutes(UUID companyId) ;
    public ResponseDto<RouteResponse> updateRoute(UUID routeId, RouteRequest request);
    public ResponseDto deleteRoute(UUID routeId);
}
