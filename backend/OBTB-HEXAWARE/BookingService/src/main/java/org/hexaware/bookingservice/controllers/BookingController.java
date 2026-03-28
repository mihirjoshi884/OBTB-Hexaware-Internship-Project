package org.hexaware.bookingservice.controllers;

import org.hexaware.bookingservice.dtos.ResponseDto;
import org.hexaware.bookingservice.services.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/booking-api/bookings/v1")
@PreAuthorize("hasAnyRole('BUS_OPERATOR','CUSTOMER')")
public class BookingController {

    @Autowired
    private TripService tripService;

    @GetMapping("/instance/seat-mappings/{instanceId}") ///booking-api/bookings/v1
    public ResponseEntity<?> getSeatMapping(@PathVariable UUID instanceId){
        ResponseDto<?> response = tripService.getSeatMappings(instanceId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

}
