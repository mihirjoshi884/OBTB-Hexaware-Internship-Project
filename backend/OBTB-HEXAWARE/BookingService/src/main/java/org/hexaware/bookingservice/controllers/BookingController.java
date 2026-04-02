package org.hexaware.bookingservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hexaware.bookingservice.dtos.ResponseDto;
import org.hexaware.bookingservice.dtos.bookingDtos.BookingInitiatedResponse;
import org.hexaware.bookingservice.dtos.bookingDtos.BookingRequestDto;
import org.hexaware.bookingservice.dtos.bookingDtos.PrimaryPassangerDetailCreationRequest;
import org.hexaware.bookingservice.services.BookingService;
import org.hexaware.bookingservice.services.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/booking-api/bookings/v1")
@PreAuthorize("hasAnyRole('BUS_OPERATOR','CUSTOMER')")
public class BookingController {

    @Autowired
    private TripService tripService;

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Autowired
    private BookingService bookingService;

    @GetMapping("/instance/seat-mappings/{instanceId}") ///booking-api/bookings/v1
    public ResponseEntity<?> getSeatMapping(@PathVariable UUID instanceId){
        ResponseDto<?> response = tripService.getSeatMappings(instanceId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/is-profile-available/{userId}")
    public ResponseEntity<?> checkProfileAvailability(@PathVariable UUID userId) {
        ResponseDto<?> response = bookingService.isPrimaryPassangerInfoAvailable(userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/create-primary-passanger")
    public ResponseEntity<?> createProfile(@RequestBody PrimaryPassangerDetailCreationRequest request) {
        ResponseDto<?> response = bookingService.createPrimaryPassanger(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * 1. Book Ticket Endpoint
     * Consumes multipart/form-data to handle both the JSON request DTO and physical ID files.
     */
    @PostMapping(value = "/book-ticket", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<BookingInitiatedResponse>> bookTicket(
            @RequestPart("request") String requestJson,
            @RequestPart("idFiles") List<MultipartFile> idFiles) {

        try {
            // Mapping the raw stringified JSON from Angular into our Request DTO
            BookingRequestDto requestDto = objectMapper.readValue(requestJson, BookingRequestDto.class);
            ResponseDto<BookingInitiatedResponse> response = bookingService.bookTicket(requestDto, idFiles);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process booking request: " + e.getMessage());
        }
    }

    /**
     * 2. Initiate Payment Endpoint
     * Called when the user clicks "Pay with Wallet" on Angular to fire the Kafka Event.
     */
    @PostMapping("/{bookingId}/initiate-payment")
    public ResponseEntity<?> initiatePayment(@PathVariable UUID bookingId) {
        ResponseDto<?> response = bookingService.initiatePayment(bookingId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * 3. Get Status Endpoint
     * Polled by Angular to know when the background Kafka flow finishes.
     */
    @GetMapping("/{bookingId}/status")
    public ResponseEntity<ResponseDto<String>> getBookingStatus(@PathVariable UUID bookingId) {
        ResponseDto<String> response = bookingService.getBookingStatus(bookingId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
