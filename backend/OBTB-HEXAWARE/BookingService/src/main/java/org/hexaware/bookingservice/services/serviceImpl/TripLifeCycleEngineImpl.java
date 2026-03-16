package org.hexaware.bookingservice.services.serviceImpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.hexaware.bookingservice.dtos.ResponseDto;
import org.hexaware.bookingservice.dtos.busDtos.BusFleetResponse;
import org.hexaware.bookingservice.dtos.busDtos.SeatLayout;
import org.hexaware.bookingservice.dtos.routeDtos.RouteResponse;
import org.hexaware.bookingservice.entites.*;
import org.hexaware.bookingservice.enums.*;
import org.hexaware.bookingservice.repositories.ArchiveRepository;
import org.hexaware.bookingservice.repositories.TripInstanceRepository;
import org.hexaware.bookingservice.repositories.TripTemplateRepository;
import org.hexaware.bookingservice.services.TripLifecycleEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TripLifeCycleEngineImpl implements TripLifecycleEngine {

    @Autowired
    private TripInstanceRepository instanceRepository;
    @Autowired
    private TripTemplateRepository templateRepository;
    @Autowired
    private ArchiveRepository archiveRepository;
    @Autowired
    private WebClient bookingWebClient;

    @Value("${busService.base-uri}")
    private String busServiceUrl;

    @Override
    @Transactional
    public void processLifecycle() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Get the stale instances that aren't archived yet
        List<TripInstance> staleInstances = instanceRepository.findByStatusAndActualArrivalBefore(
                TripStatus.SCHEDULED, now);

        for (TripInstance ti : staleInstances) {
            // 1. Create Archive Record
            TripArchive archive = new TripArchive();
            archive.setInstanceId(ti.getInstanceId());
            archive.setTemplateId(ti.getTemplate().getTemplateId());
            archive.setActualDeparture(ti.getActualDeparture());
            archive.setActualArrival(ti.getActualArrival());
            archive.setFinalFare(ti.getTemplate().getBaseFare());
            archive.setArchivedAt(LocalDateTime.now());

            archiveRepository.save(archive);

            // 2. The "Nuclear" Clean Slate
            // Because orphanRemoval=true is set in TripInstance, deleting the
            // instance automatically wipes all TripSeats from memory
            instanceRepository.delete(ti);

            // 3. One-Time Template Management
            if (ti.getTemplate().getTripType() == TripType.ONE_TIME) {
                ti.getTemplate().setActive(false);
            }
        }

        // Final flush for deletions before Part 2 starts
        instanceRepository.flush();

        // PART 2: REGULATION
        List<TripTemplate> activeRegular = templateRepository.findByIsActiveTrueAndTripType(TripType.REGULAR);

        for (TripTemplate tt : activeRegular) {
            java.time.DayOfWeek standardDay = java.time.DayOfWeek.valueOf(tt.getScheduledDay().name());
            LocalDate nextOccurrence = LocalDate.now().with(TemporalAdjusters.nextOrSame(standardDay));

            boolean exists = instanceRepository.existsByTemplate_TemplateIdAndActualDepartureBetween(
                    tt.getTemplateId(),
                    nextOccurrence.atStartOfDay(),
                    nextOccurrence.atTime(LocalTime.MAX)
            );

            if (!exists) {
                instantiate(tt, tt.getDepartureTime());
            }
        }
    }

    @Override
    @Transactional
    public TripInstance instantiate(TripTemplate template, LocalTime time) {
        TripInstance instance = new TripInstance();
        instance.setTemplate(template);

        // 1. Calculate Target Date and Start Time
        LocalDate targetDate = (template.getTripType() == TripType.ONE_TIME)
                ? template.getDepartureDate()
                : LocalDate.now().with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.valueOf(template.getScheduledDay().name())));

        // Determine start time based on TripType
        LocalTime startTime = (template.getTripType() == TripType.ONE_TIME)
                ? template.getDepartureTime()
                : template.getRegularTime();

        LocalDateTime tripStart = LocalDateTime.of(targetDate, startTime);
        instance.setActualDeparture(tripStart);
        instance.setStatus(TripStatus.SCHEDULED);

        // 2. Fetch External Data
        // Note: It's better to fetch all but keep filtering logic clear
        ResponseDto<List<BusFleetResponse>> busResponse = bookingWebClient.get()
                .uri(busServiceUrl + "/bus-api/private/v1/bus/get-buses/{companyId}", template.getCompanyId())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseDto<List<BusFleetResponse>>>() {})
                .block();

        ResponseDto<List<RouteResponse>> routeResponse = bookingWebClient.get()
                .uri(busServiceUrl + "/bus-api/private/v1/bus/routes/company/{companyId}", template.getCompanyId())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseDto<List<RouteResponse>>>() {})
                .block();

        // 3. Process Route and Stop Timings
        var route = routeResponse.getBody().stream()
                .filter(r -> r.routeId().equals(template.getRouteId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Route not found for template"));

        int standardHaltMinutes = 5;
        List<TripStopInstance> tripStops = route.stops().stream().map(stop -> {
            TripStopInstance tsi = new TripStopInstance();
            tsi.setTripInstance(instance);
            tsi.setStopName(stop.stopName());
            tsi.setStopOrder(stop.stopOrder());

            // Logic: Offset is cumulative minutes from Origin
            LocalDateTime arrivalTime = tripStart.plusMinutes(stop.timeOffsetFromOrigin());
            tsi.setArrivalTime(arrivalTime);

            // Logic: Destination doesn't "depart"
            boolean isLastStop = stop.stopOrder().equals(route.stops().size() - 1);
            if (isLastStop) {
                tsi.setDepartureTime(arrivalTime);
            } else {
                tsi.setDepartureTime(arrivalTime.plusMinutes(standardHaltMinutes));
            }
            return tsi;
        }).collect(Collectors.toList());

        instance.setStops(tripStops);

        // Set Instance Arrival to the final stop's arrival time
        if (!tripStops.isEmpty()) {
            instance.setActualArrival(tripStops.get(tripStops.size() - 1).getArrivalTime());
        }

        // 4. Seat Map Generation
        var bus = busResponse.getBody().stream()
                .filter(b -> b.busId().equals(template.getBusId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Bus not found for template"));

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<SeatLayout> layoutList = objectMapper.readValue(
                    bus.template().layoutData(), new TypeReference<List<SeatLayout>>() {});

            List<TripSeat> seats = layoutList.stream()
                    .filter(s -> !"WALKWAY".equals(s.type()))
                    .map(s -> {
                        TripSeat seat = new TripSeat();
                        seat.setSeatNumber(s.id());
                        seat.setSeatType(s.type());
                        seat.setStatus(SeatStatus.AVAILABLE);
                        seat.setTripInstance(instance);
                        return seat;
                    }).collect(Collectors.toList());

            instance.setSeatMap(seats);
            return instanceRepository.save(instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate seats", e);
        }
    }
}