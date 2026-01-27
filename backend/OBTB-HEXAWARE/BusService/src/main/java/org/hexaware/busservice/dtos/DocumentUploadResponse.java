package org.hexaware.busservice.dtos;

import org.hexaware.busservice.enums.VerificationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentUploadResponse(
        UUID busOperatorId,
        String aadharNumber,
        String aadharUrl,
        String panNumber,
        String panUrl,
        VerificationStatus status,
        LocalDateTime submittedAt,
        LocalDateTime verificationAt
) { }
