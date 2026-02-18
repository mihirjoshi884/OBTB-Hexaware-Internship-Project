package org.hexaware.busservice.dtos;

import org.hexaware.busservice.enums.VerificationStatus;

import java.util.UUID;

public record CompanyCreationResponse(
        UUID companyId,
        String companyName,
        String ownerName,
        UUID ownerId,
        VerificationStatus status
) { }
