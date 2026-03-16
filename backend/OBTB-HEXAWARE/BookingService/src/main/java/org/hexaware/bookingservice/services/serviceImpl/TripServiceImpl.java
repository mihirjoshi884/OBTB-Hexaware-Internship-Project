package org.hexaware.bookingservice.services.serviceImpl;

import jakarta.transaction.Transactional;
import org.hexaware.bookingservice.dtos.ResponseDto;
import org.hexaware.bookingservice.dtos.busDtos.BusFleetResponse;
import org.hexaware.bookingservice.dtos.routeDtos.RouteResponse;
import org.hexaware.bookingservice.dtos.tripDtos.TripCreationRequest;
import org.hexaware.bookingservice.dtos.tripDtos.TripDetails;
import org.hexaware.bookingservice.dtos.tripDtos.TripTemplateDto;
import org.hexaware.bookingservice.entites.TripInstance;
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
        TripTemplate template = new TripTemplate();
        template.setRouteId(request.routeId());
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
    public List<TripInstance> getUpcomingInstancesByRoute(UUID routeId) {
        return instanceRepository.findByTemplate_RouteIdAndStatusAndActualDepartureBetween(
                routeId, TripStatus.SCHEDULED, LocalDateTime.now(), LocalDateTime.now().plusDays(7));
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
}