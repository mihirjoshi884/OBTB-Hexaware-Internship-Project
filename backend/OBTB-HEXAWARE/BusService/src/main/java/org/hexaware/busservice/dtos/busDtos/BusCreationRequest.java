package org.hexaware.busservice.dtos.busDtos;

import org.hexaware.busservice.enums.BusType;

import java.util.UUID;

public record BusCreationRequest(
        String busName,
        BusType busType,
        UUID companyId,
        UUID templateId,
        String registrationNumber,
        String insurancePolicyNumber,
        String rcNumber
) {}
