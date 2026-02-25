package org.hexaware.busservice.dtos.staffDtos;

import org.hexaware.busservice.enums.StaffType;

import java.util.UUID;

public record BusStaffCreationRequest(
    String name,
    UUID companyId,
    int age,
    String phoneNumber,
    String driverLicenseNumber,
    StaffType staffType
) { }
