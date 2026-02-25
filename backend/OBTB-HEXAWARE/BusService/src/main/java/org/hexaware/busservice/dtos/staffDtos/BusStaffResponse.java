package org.hexaware.busservice.dtos.staffDtos;

import org.hexaware.busservice.enums.DutyType;
import org.hexaware.busservice.enums.StaffType;

import java.util.UUID;

public record BusStaffResponse(
    StaffType staffType,
    UUID staffId,
    DutyType dutyType,
    String staffName,
    UUID busId) { }
