package org.hexaware.busservice.dtos;

import java.util.UUID;

public record CompanySummaryDTO(
        String companyName,
        UUID companyId
) {}