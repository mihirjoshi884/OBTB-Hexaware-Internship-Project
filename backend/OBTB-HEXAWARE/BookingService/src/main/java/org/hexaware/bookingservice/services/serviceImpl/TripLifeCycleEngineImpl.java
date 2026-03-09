package org.hexaware.bookingservice.services.serviceImpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.hexaware.bookingservice.dtos.ResponseDto;
import org.hexaware.bookingservice.dtos.busDtos.BusFleetResponse;
import org.hexaware.bookingservice.dtos.busDtos.SeatLayout;
import org.hexaware.bookingservice.entites.TripArchive;
import org.hexaware.bookingservice.entites.TripInstance;
import org.hexaware.bookingservice.entites.TripSeat;
import org.hexaware.bookingservice.entites.TripTemplate;
import org.hexaware.bookingservice.enums.SeatStatus;
import org.hexaware.bookingservice.enums.SeatType;
import org.hexaware.bookingservice.enums.TripStatus;
import org.hexaware.bookingservice.enums.TripType;
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

        // PART 2: REGULATION (Automatic Forecasting)
        // Find all active regular schedules
        List<TripTemplate> activeRegular = templateRepository.findByIsActiveTrueAndTripType(TripType.REGULAR);

        for (TripTemplate tt : activeRegular) {
            // Calculate the date for the NEXT occurrence of the scheduled day
            // e.g., If today is Monday and schedule is FRIDAY, this returns next Friday
            LocalDate nextDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(tt.getScheduledDay()));

            // Safety Check: Does an instance already exist for this date?
            boolean exists = instanceRepository.existsByTemplate_TemplateIdAndActualDepartureBetween(
                    tt.getTemplateId(),
                    nextDate.atStartOfDay(),
                    nextDate.atTime(LocalTime.MAX)
            );

            if (!exists) {
                instantiate(tt, nextDate); // Create the new instance and seat map
            }
        }
    }

    @Override
    @Transactional
    public TripInstance instantiate(TripTemplate template, LocalDate date) {
        TripInstance instance = new TripInstance();
        instance.setTemplate(template);
        instance.setActualDeparture(LocalDateTime.of(date, template.getDepartureTime()));
        instance.setActualArrival(LocalDateTime.of(date, template.getArrivalTime()));
        instance.setStatus(TripStatus.SCHEDULED);

        // Fetch Bus Data (Reuse your logic)
        ResponseDto<List<BusFleetResponse>> busResponse = bookingWebClient.get()
                .uri(busServiceUrl + "/bus-api/private/v1/get-buses/{companyId}", template.getCompanyId())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseDto<List<BusFleetResponse>>>() {})
                .block();

        var bus = busResponse.getBody().stream()
                .filter(b -> b.busId().equals(template.getBusId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Bus not found for template"));

        // Create Fresh Seat Map for this specific date
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<SeatLayout> layoutList = objectMapper.readValue(
                    bus.template().layoutData(), new TypeReference<List<SeatLayout>>() {});

            List<TripSeat> seats = layoutList.stream()
                    .filter(s -> !"WALKWAY".equals(s.type()))
                    .map(s -> {
                        TripSeat seat = new TripSeat();
                        seat.setSeatNumber(s.id());
                        seat.setSeatType(SeatType.valueOf(s.type()));
                        seat.setStatus(SeatStatus.AVAILABLE);
                        seat.setTripInstance(instance); // Linked to Instance
                        return seat;
                    }).collect(Collectors.toList());

            instance.setSeatMap(seats);
            return instanceRepository.save(instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate seats for instance", e);
        }
    }
}