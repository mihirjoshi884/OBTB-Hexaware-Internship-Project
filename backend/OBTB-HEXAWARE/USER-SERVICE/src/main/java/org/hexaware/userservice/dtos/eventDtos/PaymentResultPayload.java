package org.hexaware.userservice.dtos.eventDtos;

import org.hexaware.userservice.enums.PaymentStatus;

import java.util.UUID;

public record PaymentResultPayload(
        UUID bookingId,
        PaymentStatus status, // "SUCCESSFUL" or "FAILED"
        String failureReason
) { }
