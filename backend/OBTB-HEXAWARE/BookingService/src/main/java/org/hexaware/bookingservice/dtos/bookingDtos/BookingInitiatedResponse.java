package org.hexaware.bookingservice.dtos.bookingDtos;

import java.util.UUID;

public record BookingInitiatedResponse(
        UUID bookingId,
        String pnr,
        String redirectUrl,
        Double amountToPay,
        String source,
        String destination
) { }
