package org.hexaware.bookingservice.dtos.eventDtos;

import java.util.UUID;

public record BookingInitiatedPayload(
        UUID bookingId,
        UUID payerUserId,
        UUID payeeUserId,
        Double amount
) { }
