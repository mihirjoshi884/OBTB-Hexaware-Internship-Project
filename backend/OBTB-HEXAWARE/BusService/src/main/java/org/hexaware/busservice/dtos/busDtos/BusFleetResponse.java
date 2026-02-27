package org.hexaware.busservice.dtos.busDtos;

import org.hexaware.busservice.dtos.companyDtos.CompanySummaryDTO;
import org.hexaware.busservice.enums.VerificationStatus;

import java.util.UUID;

public record BusFleetResponse(
        UUID busId,
        String busName,
        VerificationStatus status,
        String registrationNumber,
        CompanySummaryDTO company,
        TemplateSummaryDTO template

) {}

