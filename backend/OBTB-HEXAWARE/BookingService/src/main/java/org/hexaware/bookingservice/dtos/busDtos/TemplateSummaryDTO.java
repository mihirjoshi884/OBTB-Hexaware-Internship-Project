package org.hexaware.bookingservice.dtos.busDtos;

public record TemplateSummaryDTO(
        String templateName,
        String busType,
        String layoutData
) {}