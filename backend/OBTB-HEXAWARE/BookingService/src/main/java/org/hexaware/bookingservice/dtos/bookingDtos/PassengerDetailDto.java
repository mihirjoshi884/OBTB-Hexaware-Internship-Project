package org.hexaware.bookingservice.dtos.bookingDtos;

import org.hexaware.bookingservice.enums.IdProofType;

public record PassengerDetailDto(
        String passengerName,
        Integer age,
        String gender,
        String seatNumber,
        IdProofType idProofType,
        String idNumber
) { }
