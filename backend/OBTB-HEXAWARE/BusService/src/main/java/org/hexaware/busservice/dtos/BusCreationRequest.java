package org.hexaware.busservice.dtos;

import org.hexaware.busservice.enums.BusType;

import java.util.UUID;

public record BusCreationRequest(
        String busName,
        UUID companyId,
        UUID templateId,
        String registrationNumber,
        String driverLicenseNumber,
        String insurancePolicyNumber,
        String rcNumber
) {}
