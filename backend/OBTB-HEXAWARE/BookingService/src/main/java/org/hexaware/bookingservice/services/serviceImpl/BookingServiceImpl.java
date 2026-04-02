package org.hexaware.bookingservice.services.serviceImpl;

import jakarta.transaction.Transactional;
import org.hexaware.bookingservice.dtos.ResponseDto;
import org.hexaware.bookingservice.dtos.bookingDtos.*;
import org.hexaware.bookingservice.dtos.busDtos.CompanySummaryDTO;
import org.hexaware.bookingservice.dtos.eventDtos.BookingInitiatedPayload;
import org.hexaware.bookingservice.dtos.eventDtos.SagaEvent;
import org.hexaware.bookingservice.entites.*;
import org.hexaware.bookingservice.enums.BookingStatus;
import org.hexaware.bookingservice.enums.EventType;
import org.hexaware.bookingservice.enums.PaymentStatus;
import org.hexaware.bookingservice.enums.SeatStatus;
import org.hexaware.bookingservice.repositories.BookingRepository;
import org.hexaware.bookingservice.repositories.PrimaryPassengerDetailRepository;
import org.hexaware.bookingservice.repositories.TripInstanceRepository;
import org.hexaware.bookingservice.services.BookingService;
import org.hexaware.bookingservice.services.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.SecureRandom;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private PrimaryPassengerDetailRepository primaryPassengerDetailRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private TripInstanceRepository instanceRepository;
    @Autowired
    private WebClient bookingWebClient;
    @Autowired
    private ImageUploadService imageUploadService;
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${busService.base-uri}/bus-api/private/v1")
    private String busServiceUrl;

    @Override
    public ResponseDto<Boolean> isPrimaryPassangerInfoAvailable(UUID userId) {
        var primaryPassanger = primaryPassengerDetailRepository.findById(userId);
        if(primaryPassanger.isEmpty()){
            return new ResponseDto<>(false, 404, "no primary passanger information was found, please create a primary passanger");
        }
        return new ResponseDto<>(true,200,"primary passanger found");
    }

    @Override
    public ResponseDto<PrimaryPassangerDetailDto> createPrimaryPassanger(PrimaryPassangerDetailCreationRequest request) {
        var primaryPassanger = new PrimaryPassangerDetail(
                request.userId(),
                request.name(),
                request.email(),
                request.phone(),
                request.emergencyContactName(),
                request.emergencyContact(),
                new ArrayList<>()
        );
        var savedPrimmaryPassanger = primaryPassengerDetailRepository.save(primaryPassanger);
        var response = new PrimaryPassangerDetailDto(
                savedPrimmaryPassanger.getUserId(),
                savedPrimmaryPassanger.getName(),
                savedPrimmaryPassanger.getEmail()
        );
        return new ResponseDto<>(response,200,"user saved");
    }


    @Override
    @Transactional
    public ResponseDto<BookingInitiatedResponse> bookTicket(BookingRequestDto request, List<MultipartFile> idFiles) {
        var booking = new Booking();
        var primaryPassenger = primaryPassengerDetailRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("primary passenger not found with id: " + request.userId()));
        var instance = instanceRepository.findById(request.tripInstanceId())
                .orElseThrow(() -> new RuntimeException("instance not found with the id: " + request.tripInstanceId()));
        var stopDetails = findStopDetails(instance, request.source(), request.destination());

        var coPassengerDtos = request.passengers();
        int count = coPassengerDtos.size();

        // 1. Process files and map DTOs to Entities
        List<Passangers> entitiesList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            PassengerDetailDto dto = coPassengerDtos.get(i);
            MultipartFile file = idFiles.get(i);

            // Generate manual UUID for Cloudinary folder paths
            UUID generatedId = UUID.randomUUID();

            String idUrl;
            try {
                Map uploadResult = imageUploadService.uploadIdproof(file, generatedId);
                idUrl = (String) uploadResult.get("idProofUrl");
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload ID proof for passenger: " + dto.passengerName(), e);
            }

            Passangers passengerEntity = mapToPassenger(dto, idUrl, generatedId);
            passengerEntity.setPrimaryPassengerDetail(primaryPassenger); // Set back-reference

            // 2. SOFT LOCK THE SEAT
            String seatNum = dto.seatNumber();
            TripSeat seat = instance.getSeatMap().stream()
                    .filter(s -> s.getSeatNumber().equals(seatNum))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Seat " + seatNum + " not found on this bus"));

            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new RuntimeException("Seat " + seatNum + " is no longer available!");
            }
            seat.setStatus(SeatStatus.LOCKED);
            seat.setLockedAt(LocalDateTime.now());

            entitiesList.add(passengerEntity);
        }

        // Add to mapped list on primary passenger
        primaryPassenger.getCoPassengers().addAll(entitiesList);

        // 3. Fill Booking Entity
        String pnr = pnrGenerator(6);
        booking.setPnrNumber(pnr);
        booking.setPrimaryPassengerDetail(primaryPassenger);
        booking.setTrip(instance); // Don't forget to link the trip!
        booking.setSource(stopDetails.source());
        booking.setSourceArrival(stopDetails.sourceArrival());
        booking.setSourceDeparture(stopDetails.sourceDeparture());
        booking.setDestination(stopDetails.destination());
        booking.setDestinationArrival(stopDetails.destinationArrival());
        booking.setDestinationDeparture(stopDetails.destinationDeparture());
        booking.setBookingStatus(BookingStatus.WAITING);
        booking.setPaymentStatus(PaymentStatus.PENDING);

        Double amountToPay = instance.getTemplate().getBaseFare() * count;
        booking.setAmountPaid(amountToPay);

        var savedBookingInstance = bookingRepository.save(booking);
        instanceRepository.save(instance); // Save seat status updates

        var companyDetails = fetchCompanyDetails(instance.getTemplate().getCompanyId());

        // Formulate a proper redirect URL referencing your checkout route
        String redirectUrl = "http://localhost:4200/payment?bookingId=" + savedBookingInstance.getBookingId();

        var responseBody = new BookingInitiatedResponse(
                savedBookingInstance.getBookingId(),
                savedBookingInstance.getPnrNumber(),
                redirectUrl,
                amountToPay,
                savedBookingInstance.getSource(),
                savedBookingInstance.getDestination()
        );

        return new ResponseDto<>(responseBody, 200, "proceed for payment");
    }

    @Override
    public ResponseDto<Boolean> initiatePayment(UUID bookingId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getBookingStatus() != BookingStatus.WAITING) {
            throw new RuntimeException("Payment cannot be initiated for booking in status: " + booking.getBookingStatus());
        }

        var companyDetail = fetchCompanyDetails(booking.getTrip().getTemplate().getCompanyId());
        // 🔥 Trigger Saga flow HERE instead of bookTicket
        BookingInitiatedPayload payload = new BookingInitiatedPayload(
                booking.getBookingId(),
                booking.getPrimaryPassengerDetail().getUserId(),
                companyDetail.ownerId(), // Assuming companyId is the payee
                booking.getAmountPaid()
        );

        SagaEvent<BookingInitiatedPayload> sagaEvent = new SagaEvent<>(
                EventType.BOOKING_INITIATED,
                payload,
                LocalDateTime.now()
        );

        kafkaTemplate.send("booking-initiated-topic", sagaEvent);

        return new ResponseDto<>(true, 200, "Payment processing initiated via Kafka.");
    }

    @Override
    public ResponseDto<String> getBookingStatus(UUID bookingId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        // Return the current status (WAITING, CONFIRMED, or FAILED)
        return new ResponseDto<>(booking.getBookingStatus().toString(), 200, "Current booking status");
    }


    // Refactored to map and set the manual UUID
    private Passangers mapToPassenger(PassengerDetailDto passengerDetailDto, String idUrl, UUID generatedId) {
        var passenger = new Passangers();
        passenger.setPassengerId(generatedId);
        passenger.setPassengerName(passengerDetailDto.passengerName());
        passenger.setAge(passengerDetailDto.age());
        passenger.setGender(passengerDetailDto.gender());
        passenger.setSeatNumber(passengerDetailDto.seatNumber()); // Added mapping for seat number
        passenger.setIdNumber(passengerDetailDto.idNumber());
        passenger.setIdProofType(passengerDetailDto.idProofType());
        passenger.setIdUrl(idUrl);
        return passenger;
    }

    private CompanySummaryDTO fetchCompanyDetails(UUID companyId) {
        ParameterizedTypeReference<ResponseDto<CompanySummaryDTO>> companyResponseType = new ParameterizedTypeReference<>() {};
        var companyResponse = bookingWebClient.get()
                .uri(busServiceUrl + "/company/get-company-details/{companyId}", companyId)
                .retrieve()
                .bodyToMono(companyResponseType)
                .block();
        return companyResponse.getBody();
    }

    private String pnrGenerator(int length) {
        String ALPHA_NUMERIC = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
        SecureRandom RANDOM = new SecureRandom();
        StringBuilder pnr = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            pnr.append(ALPHA_NUMERIC.charAt(RANDOM.nextInt(ALPHA_NUMERIC.length())));
        }
        return pnr.toString();
    }

    private StopDetailsDto findStopDetails(TripInstance instance, String source, String destination) {
        List<TripStopInstance> stops = instance.getStops();

        String trimmedSource = source.trim();
        String trimmedDest = destination.trim();

        // 1. Resolve the Source
        TripStopInstance sourceStop = stops.stream()
                .filter(stop -> stop.getStopName().trim().equalsIgnoreCase(trimmedSource) ||
                        stop.getStopName().trim().toLowerCase().contains(trimmedSource.toLowerCase()))
                .findFirst()
                .orElse(null);

        // 2. Resolve the Destination
        TripStopInstance destStop = stops.stream()
                .filter(stop -> stop.getStopName().trim().equalsIgnoreCase(trimmedDest) ||
                        stop.getStopName().trim().toLowerCase().contains(trimmedDest.toLowerCase()))
                .findFirst()
                .orElse(null);

        LocalDateTime sourceArrival = null;
        LocalDateTime sourceDeparture = null;
        LocalDateTime destArrival = null;
        LocalDateTime destDeparture = null;

        // 3. Fallback logic for Source (Check if it's the absolute start terminal)
        if (sourceStop != null) {
            sourceArrival = (sourceStop.getStopOrder() == 0) ? null : sourceStop.getArrivalTime();
            sourceDeparture = sourceStop.getDepartureTime();
        } else if (instance.getTemplate() != null &&
                instance.getTemplate().getSource().equalsIgnoreCase(trimmedSource)) {
            // Fallback to the TripTemplate's master origin
            sourceArrival = null; // Origin has no arrival
            sourceDeparture = instance.getActualDeparture(); // Master departure time
        } else {
            throw new RuntimeException("Source stop '" + source + "' not found. Available stops: " +
                    stops.stream().map(TripStopInstance::getStopName).toList());
        }

        // 4. Fallback logic for Destination (Check if it's the absolute final terminal)
        if (destStop != null) {
            destArrival = destStop.getArrivalTime();
            boolean isTerminalStop = destStop.getStopOrder().equals(stops.size() - 1);
            destDeparture = isTerminalStop ? null : destStop.getDepartureTime();
        } else if (instance.getTemplate() != null &&
                instance.getTemplate().getDestination().equalsIgnoreCase(trimmedDest)) {
            // Fallback to the TripTemplate's master destination
            destArrival = instance.getActualArrival(); // Master arrival time
            destDeparture = null; // Final stop has no departure
        } else {
            throw new RuntimeException("Destination stop '" + destination + "' not found. Available stops: " +
                    stops.stream().map(TripStopInstance::getStopName).toList());
        }

        return new StopDetailsDto(
                source,
                sourceArrival,
                sourceDeparture,
                destination,
                destArrival,
                destDeparture
        );
    }
}
