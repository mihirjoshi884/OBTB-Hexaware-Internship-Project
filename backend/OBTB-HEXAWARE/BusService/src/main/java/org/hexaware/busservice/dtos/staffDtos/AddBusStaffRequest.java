package org.hexaware.busservice.dtos.staffDtos;

import org.hexaware.busservice.enums.DutyType;

import java.util.UUID;

public record AddBusStaffRequest(
        UUID staffId,
        UUID busId,
        String staffName,
        DutyType dutyType
) { }
