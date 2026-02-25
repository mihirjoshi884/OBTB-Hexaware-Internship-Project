package org.hexaware.busservice.dtos.staffDtos;

import org.hexaware.busservice.enums.StaffType;

import java.util.UUID;

public record BusStaffCreationResponse(
        UUID staffId,
        String name,
        String phoneNumber,
        String driverLicenseNumber,
        StaffType staffType
) { }
