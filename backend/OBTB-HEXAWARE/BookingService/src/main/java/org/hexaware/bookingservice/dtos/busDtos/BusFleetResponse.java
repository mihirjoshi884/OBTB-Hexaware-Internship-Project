package org.hexaware.bookingservice.dtos.busDtos;
import org.hexaware.bookingservice.enums.VerificationStatus;

import java.util.UUID;

public record BusFleetResponse(
        UUID busId,
        String busName,
        VerificationStatus status,
        String registrationNumber,
        CompanySummaryDTO company,
        TemplateSummaryDTO template

) {}

