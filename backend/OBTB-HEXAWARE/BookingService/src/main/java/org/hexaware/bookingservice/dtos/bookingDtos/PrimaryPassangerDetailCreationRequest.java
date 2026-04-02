package org.hexaware.bookingservice.dtos.bookingDtos;

import java.util.UUID;

public record PrimaryPassangerDetailCreationRequest (
        UUID userId,
        String name,
        String phone,
        String email,
        String emergencyContactName,
        String emergencyContact
){ }
