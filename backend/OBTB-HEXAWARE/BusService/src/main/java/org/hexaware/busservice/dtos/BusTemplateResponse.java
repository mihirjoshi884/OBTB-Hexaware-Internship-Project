package org.hexaware.busservice.dtos;

import org.hexaware.busservice.enums.BusType;

import java.util.UUID;

public record BusTemplateResponse(
        UUID templateId,
        String templateName,
        BusType busType,
        Integer totalSeats,
        String layoutData
) { }