package org.hexaware.bookingservice.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/booking-api/bookings/v1")
@PreAuthorize("hasAnyRole('BUS_OPERATOR','CUSTOMER')")
public class BookingController {


}
