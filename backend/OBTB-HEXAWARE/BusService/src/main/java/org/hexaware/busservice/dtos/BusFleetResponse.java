package org.hexaware.busservice.dtos;

import java.util.UUID;

public record BusFleetResponse(
        UUID busId,
        String busName,
        String registrationNumber,
        CompanySummaryDTO company,
        TemplateSummaryDTO template
) {}

