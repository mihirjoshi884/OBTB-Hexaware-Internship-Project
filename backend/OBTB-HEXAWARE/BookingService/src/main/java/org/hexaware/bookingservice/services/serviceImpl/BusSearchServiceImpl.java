package org.hexaware.bookingservice.services.serviceImpl;


import org.hexaware.bookingservice.dtos.busDtos.BusFleetResponse;
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
import java.time.LocalTime;
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
        // 1. Trim the source and destination to remove trailing spaces/formatting
        String cleanSource = request.source().trim().toLowerCase();
        String cleanDest = request.destination().trim().toLowerCase();

        LocalDateTime departureDateTime = LocalDateTime.parse(request.departureDate() + "T00:00:00");
        List<TripSearchResponseDto> allResults = new ArrayList<>();

        // 2. Fetch Outbound leg using cleaned strings
        allResults.addAll(searchJourney(
                new FetchRoute(cleanSource, cleanDest),
                departureDateTime,
                "OUTBOUND"
        ));

        // 3. Fetch Inbound leg if Round Trip
        if (JourneyType.ROUND_TRIP.equals(request.journeyType()) && request.returnDate() != null) {
            LocalDateTime returnDateTime = LocalDateTime.parse(request.returnDate() + "T00:00:00");
            allResults.addAll(searchJourney(
                    new FetchRoute(cleanDest, cleanSource),
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

        // FIX: Define the full day range
        LocalDateTime startOfDay = searchDate.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = searchDate.toLocalDate().atTime(LocalTime.MAX);

        // FIX: Pass the range to the repository
        List<TripInstance> instances = repository.findAvailableTripsInRange(templateIds, startOfDay, endOfDay);

        return instances.stream()
                .map(instance -> {
                    String routeName = routeNameMap.get(instance.getTemplate().getRouteId());
                    var busResponse = fetchBusFromService(instance.getTemplate().getCompanyId(),instance.getTemplate().getBusId());
                    return mapInstancesToSearchResponseDto(instance, routeName, busResponse.busName(), direction);
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

    private BusFleetResponse fetchBusFromService(UUID companyId, UUID busId) {
        String fullUrl = busServiceUrl + "/bus/get-buses/{companyId}";
        ResponseDto<List<BusFleetResponse>> response = bookingWebClient.get()
                .uri(fullUrl, companyId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseDto<List<BusFleetResponse>>>() {})
                .block();

        return response.getBody().stream()
                .filter(b -> b.busId().equals(busId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Bus details not found"));
    }


    private TripSearchResponseDto mapInstancesToSearchResponseDto(TripInstance instance, String routeName,String busName, String direction){
        return new TripSearchResponseDto(
                instance.getInstanceId(),
                routeName,
                busName,
                instance.getTemplate().getSource(),
                instance.getTemplate().getDestination(),
                instance.getActualDeparture(),
                instance.getActualArrival(),
                instance.getTemplate().getBaseFare(),
                instance.getSeatMap().stream().count(),
                instance.getStatus(),
                direction
        );
    }
}
