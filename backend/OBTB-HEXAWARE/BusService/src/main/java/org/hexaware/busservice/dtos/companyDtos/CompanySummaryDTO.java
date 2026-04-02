package org.hexaware.busservice.dtos.companyDtos;

import java.util.UUID;

public record CompanySummaryDTO(
        String companyName,
        UUID companyId,
        UUID ownerId
) {}