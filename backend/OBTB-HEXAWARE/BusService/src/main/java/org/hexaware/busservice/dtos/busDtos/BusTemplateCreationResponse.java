package org.hexaware.busservice.dtos.busDtos;

import java.util.UUID;

public record BusTemplateCreationResponse (
        UUID templateId,
        String templateName,
        String layoutData,
        UUID layoutId,
        Integer totalSeats
) {}
