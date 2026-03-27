package org.hexaware.bookingservice.services.serviceImpl;

import jakarta.transaction.Transactional;
import org.apache.logging.log4j.CloseableThreadContext;
import org.hexaware.bookingservice.dtos.ResponseDto;
import org.hexaware.bookingservice.dtos.busDtos.BusFleetResponse;
import org.hexaware.bookingservice.dtos.instanceDtos.InstanceDto;
import org.hexaware.bookingservice.dtos.instanceDtos.StopsDto;
import org.hexaware.bookingservice.dtos.routeDtos.RouteResponse;
import org.hexaware.bookingservice.dtos.tripDtos.TripCreationRequest;
import org.hexaware.bookingservice.dtos.tripDtos.TripDetails;
import org.hexaware.bookingservice.dtos.tripDtos.TripTemplateDto;
import org.hexaware.bookingservice.entites.TripInstance;
import org.hexaware.bookingservice.entites.TripSeat;
import org.hexaware.bookingservice.entites.TripStopInstance;
import org.hexaware.bookingservice.entites.TripTemplate;
import org.hexaware.bookingservice.enums.DayOfWeek;
import org.hexaware.bookingservice.enums.TripStatus;
import org.hexaware.bookingservice.enums.TripType;
import org.hexaware.bookingservice.repositories.TripInstanceRepository;
import org.hexaware.bookingservice.repositories.TripTemplateRepository;
import org.hexaware.bookingservice.services.TripLifecycleEngine;
import org.hexaware.bookingservice.services.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TripServiceImpl implements TripService {

    @Autowired
    private TripTemplateRepository templateRepository;
    @Autowired private TripInstanceRepository instanceRepository;
    @Autowired
    private TripLifecycleEngine lifecycleEngine;
    @Autowired
    private WebClient bookingWebClient;
    @Value("${busService.base-uri}")
    private String busServiceUrl;

    @Override
    @Transactional
    public ResponseDto<TripDetails> createTrip(TripCreationRequest request) {
        RouteResponse routeDetails = fetchRouteFromService(request.routeId());

        TripTemplate template = new TripTemplate();
        template.setRouteId(request.routeId());
        template.setSource(routeDetails.origin()); // Matches your "Source" field
        template.setDestination(routeDetails.destination());
        template.setBusId(request.busId());
        template.setCompanyId(request.companyId());
        template.setTripType(request.tripType());
        template.setBaseFare(request.baseFare());
        template.setActive(true);

        // Determine which time to use for the initial instantiation
        LocalTime startTime;

        if (request.tripType().equals(TripType.ONE_TIME)) {
            startTime = LocalTime.parse(request.departureTime());
            template.setDepartureTime(startTime);
            template.setArrivalTime(LocalTime.parse(request.arrivalTime()));
            template.setDepartureDate(LocalDate.parse(request.departureDate()));
            template.setArrivalDate(LocalDate.parse(request.arrivalDate()));
        } else {
            startTime = LocalTime.parse(request.regularDepartureTime());
            template.setScheduledDay(DayOfWeek.valueOf(request.scheduledDay().toUpperCase()));
            template.setRegularTime(startTime);
        }

        TripTemplate savedTemplate = templateRepository.save(template);

        // 2. Spawn the Initial Instance
        // Pass 'startTime' which we already parsed above safely
        TripInstance firstInstance = lifecycleEngine.instantiate(savedTemplate, startTime);

        // 3. Fetch Bus Details for the response
        var busResponse = fetchBusFromService(savedTemplate.getCompanyId(), savedTemplate.getBusId());

        TripDetails details = new TripDetails(
                firstInstance.getInstanceId(),
                savedTemplate.getBusId(),
                firstInstance.getActualArrival(),
                firstInstance.getActualDeparture(),
                savedTemplate.getBaseFare(),
                busResponse
        );

        return new ResponseDto<>(details, 201, "Template and initial instance created successfully");
    }

    @Override
    public ResponseDto<List<TripTemplateDto>> getTemplatesByCompany(UUID companyId) {
        List<TripTemplate> templates = templateRepository.findByCompanyId(companyId);
        if (templates.isEmpty()) return (ResponseDto<List<TripTemplateDto>>) Collections.emptyList();

        Set<UUID> busIds = templates.stream().map(TripTemplate::getBusId).collect(Collectors.toSet());
        Set<UUID> routeIds = templates.stream().map(TripTemplate::getRouteId).collect(Collectors.toSet());

        ParameterizedTypeReference<ResponseDto<List<BusFleetResponse>>> busResponseType =
                new ParameterizedTypeReference<>() {};

        ResponseDto<List<BusFleetResponse>> busResponse = bookingWebClient.post()
                .uri(busServiceUrl + "/bus-api/private/v1/bus/get-buses-bulk")
                .bodyValue(busIds) // Use .bodyValue for direct objects
                .retrieve()
                .bodyToMono(busResponseType)
                .block();

        ParameterizedTypeReference<ResponseDto<List<RouteResponse>>> routeResponseType =
                new ParameterizedTypeReference<>() {};

        ResponseDto<List<RouteResponse>> routeResponse = bookingWebClient.post()
                .uri(busServiceUrl+"/bus-api/private/v1/bus/routes/get-routes-bulk")
                .bodyValue(routeIds)
                .retrieve()
                .bodyToMono(routeResponseType)
                .block();

        Map<UUID, BusFleetResponse> busMap = (busResponse != null && busResponse.getBody() != null)
                ? busResponse.getBody().stream().collect(Collectors.toMap(BusFleetResponse::busId, b -> b))
                : Collections.emptyMap();

        Map<UUID, RouteResponse> routeMap = (routeResponse != null && routeResponse.getBody() != null)
                ? routeResponse.getBody().stream().collect(Collectors.toMap(RouteResponse::routeId, r -> r))
                : Collections.emptyMap();


        List<TripTemplateDto> dtoList = templates.stream().map(t -> {
            BusFleetResponse busInfo = busMap.get(t.getBusId());
            RouteResponse routeInfo = routeMap.get(t.getRouteId());

            // Construct the record with all fields at once
            return new TripTemplateDto(
                    t.getTemplateId(),
                    routeInfo.routeId(),
                    routeInfo.routeName(),
                    routeInfo.origin(),
                    routeInfo.destination(),
                    busInfo.busId(),
                    busInfo.busName(),
                    busInfo.company().companyId(),
                    busInfo.company().companyName(),
                    t.getBaseFare(),
                    t.getTripType(),
                    t.getScheduledDay(),
                    t.getRegularTime(),
                    t.getDepartureTime(),
                    t.getArrivalTime(),
                    t.getDepartureDate(),
                    t.getArrivalDate(),
                    t.isActive()
            );
        }).collect(Collectors.toList());

        return new ResponseDto<>(dtoList, 200, "Templates retrieved and enriched successfully");
    }



    @Override
    @Transactional
    public void toggleTemplateStatus(UUID templateId, boolean active) {
        TripTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        template.setActive(active);
        templateRepository.save(template);
    }

    @Override
    @Transactional
    public void deleteTemplate(UUID templateId) {
        // Logic: When a blueprint is deleted, we should also cancel
        // future 'SCHEDULED' instances, but preserve 'COMPLETED' ones for history.
        instanceRepository.deleteByTemplate_TemplateIdAndStatus(templateId, TripStatus.SCHEDULED);
        templateRepository.deleteById(templateId);
    }

    private BusFleetResponse fetchBusFromService(UUID companyId, UUID busId) {
        String fullUrl = busServiceUrl + "/bus-api/private/v1/bus/get-buses/{companyId}";
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

    @Override
    public ResponseDto<List<InstanceDto>> getUpcomingInstancesByTemplateIds(List<UUID> templateIds) {

        if (templateIds == null || templateIds.isEmpty()) {
            return new ResponseDto<>(Collections.emptyList(), 200, "No IDs provided");
        }
        var results = instanceRepository.findAllByTemplate_TemplateIds(templateIds, TripStatus.SCHEDULED);
        var instanceResult = results.stream().map(this::mapInstanceToInstanceDto).collect(Collectors.toList());
        var response = new  ResponseDto<>(instanceResult, 200, "Instances retrieved successfully");
        return response;
    }

    private InstanceDto mapInstanceToInstanceDto(TripInstance instance) {
        List<StopsDto> stops = new ArrayList<>();
        for(TripStopInstance stop : instance.getStops()) {
            stops.add(new StopsDto(
                    stop.getId(),
                    stop.getStopName(),
                    stop.getStopOrder(),
                    stop.getArrivalTime(),
                    stop.getDepartureTime()
            ));
        }
        return new InstanceDto(
                instance.getInstanceId(),
                instance.getTemplate().getTemplateId(),
                instance.getTemplate().getSource(),
                instance.getTemplate().getDestination(),
                instance.getActualDeparture(),
                instance.getActualArrival(),
                stops,
                instance.getStatus()

        );
    }
    private RouteResponse fetchRouteFromService(UUID routeId) {
        // 1. Point to the existing bulk endpoint
        String bulkRouteUrl = busServiceUrl + "/bus-api/private/v1/bus/routes/get-routes-bulk";

        // 2. Wrap the single ID in a List to satisfy the @RequestBody List<UUID>
        List<UUID> routeIds = Collections.singletonList(routeId);

        // 3. Define the type reference for List<RouteResponse>
        ParameterizedTypeReference<ResponseDto<List<RouteResponse>>> responseType =
                new ParameterizedTypeReference<>() {};

        // 4. Make the POST call
        ResponseDto<List<RouteResponse>> response = bookingWebClient.post()
                .uri(bulkRouteUrl)
                .bodyValue(routeIds)
                .retrieve()
                .bodyToMono(responseType)
                .block();

        // 5. Extract the first (and only) route from the list
        if (response == null || response.getBody() == null || response.getBody().isEmpty()) {
            throw new RuntimeException("Route details not found for ID: " + routeId);
        }

        return response.getBody().get(0);
    }
}