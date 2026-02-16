package org.hexaware.busservice.dtos;

import java.util.UUID;

public record BusTemplateCreationResponse (
        UUID templateId,
        String templateName,
        String layoutData,
        Integer totalSeats
) {}
