package org.hexaware.busservice.dtos;

import org.hexaware.busservice.enums.BusType;

import java.util.UUID;

public record BusTemplateCreationRequest(
        String templateName,
        UUID layoutId,   // The ID of the blueprint chosen (LayoutTemplate)
        BusType busType, // The specific service type (e.g., AC_SLEEPER)
        Integer totalSeats,
        UUID ownerId
) {}
