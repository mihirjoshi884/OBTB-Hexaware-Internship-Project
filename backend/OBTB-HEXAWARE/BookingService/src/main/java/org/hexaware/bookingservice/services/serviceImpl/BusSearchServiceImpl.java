package org.hexaware.bookingservice.services.serviceImpl;


import org.hexaware.bookingservice.dtos.routeDtos.RouteResponse;
import org.hexaware.bookingservice.dtos.searchDtos.FetchRoute;
import org.hexaware.bookingservice.entites.TripInstance;
import org.hexaware.bookingservice.entites.TripTemplate;
import org.hexaware.bookingservice.enums.JourneyType;
import org.hexaware.bookingservice.repositories.TripTemplateRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.transaction.annotation.Transactional;
import org.hexaware.bookingservice.dtos.ResponseDto;
import org.hexaware.bookingservice.dtos.searchDtos.SearchRequestDto;
import org.hexaware.bookingservice.dtos.searchDtos.TripSearchResponseDto;
import org.hexaware.bookingservice.repositories.TripInstanceRepository;
import org.hexaware.bookingservice.services.BusSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BusSearchServiceImpl implements BusSearchService {

    @Autowired
    private TripInstanceRepository repository;
    @Autowired
    private TripTemplateRepository tripTemplateRepository;

    @Autowired
    private WebClient bookingWebClient;

    @Value("${busService.base-uri}/bus-api/private/v1")
    private String busServiceUrl;

    @Transactional(readOnly = true)
    @Override
    public ResponseDto<List<TripSearchResponseDto>> searchBuses(SearchRequestDto request) {
        LocalDateTime departureDateTime = LocalDateTime.parse(request.departureDate() + "T00:00:00");
        List<TripSearchResponseDto> allResults = new ArrayList<>();

        // 1. Fetch Outbound leg
        allResults.addAll(searchJourney(
                new FetchRoute(request.source(), request.destination()),
                departureDateTime,
                "OUTBOUND"
        ));

        // 2. Fetch Inbound leg if Round Trip
        if (JourneyType.ROUND_TRIP.equals(request.journeyType()) && request.returnDate() != null) {
            LocalDateTime returnDateTime = LocalDateTime.parse(request.returnDate() + "T00:00:00");
            allResults.addAll(searchJourney(
                    new FetchRoute(request.destination(), request.source()),
                    returnDateTime,
                    "INBOUND"
            ));
        }

        return new ResponseDto<>(allResults, 200, "success");
    }

    private List<TripSearchResponseDto> searchJourney(FetchRoute fetchRequest, LocalDateTime searchDate, String direction) {
        var routesResults = fetchRouteDetails(fetchRequest);

        if (routesResults == null || routesResults.getBody() == null || routesResults.getBody().isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, String> routeNameMap = routesResults.getBody().stream()
                .collect(Collectors.toMap(RouteResponse::routeId, RouteResponse::routeName));

        List<UUID> routeIds = new ArrayList<>(routeNameMap.keySet());

        List<TripTemplate> activeTemplates = tripTemplateRepository.findActiveTripTemplatesByRouteIds(routeIds);
        if (activeTemplates.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> templateIds = activeTemplates.stream().map(TripTemplate::getTemplateId).toList();
        List<TripInstance> instances = repository.findAvailableTrips(templateIds, searchDate);

        return instances.stream()
                .map(instance -> {
                    String routeName = routeNameMap.get(instance.getTemplate().getRouteId());
                    return mapInstancesToSearchResponseDto(instance, routeName, direction);
                })
                .collect(Collectors.toList());
    }

    private ResponseDto<List<RouteResponse>> fetchRouteDetails(FetchRoute fetchRequest){
        String routeUrl = busServiceUrl+"/find/routes";
        ParameterizedTypeReference<ResponseDto<List<RouteResponse>>> routeResponseType = new ParameterizedTypeReference<>() {};
        var routes = bookingWebClient.post()
                .uri(routeUrl)
                .bodyValue(fetchRequest)
                .retrieve()
                .bodyToMono(routeResponseType)
                .block();
        return routes;
    }

    private TripSearchResponseDto mapInstancesToSearchResponseDto(TripInstance instance, String routeName, String direction){
        return new TripSearchResponseDto(
                instance.getInstanceId(),
                routeName,
                instance.getActualDeparture(),
                instance.getActualArrival(),
                instance.getTemplate().getBaseFare(),
                instance.getSeatMap().stream().count(),
                instance.getStatus(),
                direction
        );
    }
}
