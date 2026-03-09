package org.hexaware.bookingservice.dtos.busDtos;

import java.util.UUID;

public record CompanySummaryDTO(
        String companyName,
        UUID companyId
) {}