package org.hexaware.busservice.dtos.busDtos;

public record TemplateSummaryDTO(
        String templateName,
        String busType,
        String layoutData
) {}