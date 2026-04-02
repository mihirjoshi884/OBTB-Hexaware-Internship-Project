package org.hexaware.bookingservice.dtos.eventDtos;



import org.hexaware.bookingservice.enums.PaymentStatus;

import java.util.UUID;

public record PaymentResultPayload(
        UUID bookingId,
        PaymentStatus status, // "SUCCESSFUL" or "FAILED"
        String failureReason
) { }
