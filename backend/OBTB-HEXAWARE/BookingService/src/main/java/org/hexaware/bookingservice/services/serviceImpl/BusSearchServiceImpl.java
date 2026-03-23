package org.hexaware.bookingservice.services.serviceImpl;


import org.hexaware.bookingservice.dtos.routeDtos.RouteResponse;
import org.hexaware.bookingservice.dtos.searchDtos.FetchRoute;
import org.hexaware.bookingservice.entites.TripInstance;
import org.hexaware.bookingservice.entites.TripTemplate;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
//        LocalDateTime travelDate = LocalDateTime.parse(request.departureDate() + "T00:00:00");
        var routesResults = fetchRouteDetails(new FetchRoute(request.source(), request.destination()));

        Map<UUID, String> routeNameMap = routesResults.getBody().stream()
                .collect(Collectors.toMap(RouteResponse::routeId, RouteResponse::routeName));
        var routesIds = new ArrayList<>(routeNameMap.keySet());

        List<TripTemplate> activeTemplates = tripTemplateRepository.findActiveTripTemplatesByRouteIds(routesIds);

        var templateIds = activeTemplates.stream().map(TripTemplate::getTemplateId).toList();
        List<TripInstance> instances = repository.findAvailableTrips(templateIds,
                LocalDateTime.parse(request.departureDate() + "T00:00:00"));


        var response = instances.stream().map(
                instance -> {
                    String routeName = routeNameMap.get(instance.getTemplate().getRouteId());
                    return mapInstancesToSearchResponseDto(instance,routeName);
                }).toList();

        return new ResponseDto<>(response,200,"success");
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

    private TripSearchResponseDto mapInstancesToSearchResponseDto(TripInstance instance, String routeName){
        return new TripSearchResponseDto(
                instance.getInstanceId(),
                routeName,
                instance.getActualDeparture(),
                instance.getActualArrival(),
                instance.getTemplate().getBaseFare(),
                instance.getSeatMap().stream().count(),
                instance.getStatus()
        );
    }
}
