package org.hexaware.bookingservice.dtos.bookingDtos;

import java.util.UUID;

public record PrimaryPassangerDetailDto(
        UUID userId,
        String name,
        String email
) { }
