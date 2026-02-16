package org.hexaware.busservice.dtos;

public record BusTemplateCreationRequest(
        String templateName,
        String layoutData,
        Integer totalSeats
) { }
