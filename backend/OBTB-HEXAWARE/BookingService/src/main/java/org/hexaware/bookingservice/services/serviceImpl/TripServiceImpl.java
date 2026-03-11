package org.hexaware.bookingservice.services.serviceImpl;

import jakarta.transaction.Transactional;
import org.hexaware.bookingservice.dtos.ResponseDto;
import org.hexaware.bookingservice.dtos.busDtos.BusFleetResponse;
import org.hexaware.bookingservice.dtos.tripDtos.TripCreationRequest;
import org.hexaware.bookingservice.dtos.tripDtos.TripDetails;
import org.hexaware.bookingservice.entites.TripInstance;
import org.hexaware.bookingservice.entites.TripTemplate;
import org.hexaware.bookingservice.enums.DayOfWeek;
import org.hexaware.bookingservice.enums.TripStatus;
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
import java.util.List;
import java.util.UUID;

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
        // 1. Create the Permanent Template (The Blueprint)
        TripTemplate template = new TripTemplate();
        template.setRouteId(request.routeId());
        template.setBusId(request.busId());
        template.setCompanyId(request.companyId());
        template.setTripType(request.tripType());
        template.setBaseFare(request.baseFare());

        // Extract recurring schedule from the request
        template.setScheduledDay(DayOfWeek.valueOf(request.scheduledDay().toUpperCase()));
        template.setDepartureTime(LocalTime.parse(request.departureTime()));
        template.setArrivalTime(LocalTime.parse(request.arrivalTime()));
        template.setActive(true);
        template.setDepartureDate(LocalDate.parse(request.departureDate()));
        template.setArrivalDate(LocalDate.parse(request.arrivalDate()));

        TripTemplate savedTemplate = templateRepository.save(template);

        // 2. Spawn the Initial Instance (The first bookable journey)
        // We pass the LocalDate of the first departure to the engine
        TripInstance firstInstance = lifecycleEngine.instantiate(
                savedTemplate,
                LocalDate.parse(request.departureTime())
        );
        var busResponse = fetchBusFromService(savedTemplate.getCompanyId(), savedTemplate.getBusId());
        // 3. Return the Details of the specific Instance created
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
    public List<TripTemplate> getTemplatesByCompany(UUID companyId) {
        return templateRepository.findByCompanyId(companyId);
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