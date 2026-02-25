package org.hexaware.busservice.dtos.busDtos;

import org.hexaware.busservice.dtos.companyDtos.CompanySummaryDTO;

import java.util.UUID;

public record BusFleetResponse(
        UUID busId,
        String busName,
        String registrationNumber,
        CompanySummaryDTO company,
        TemplateSummaryDTO template

) {}

