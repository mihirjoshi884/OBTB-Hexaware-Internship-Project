package org.hexaware.busservice.dtos.documentDtos;

import java.util.UUID;

public record DocumentUploadRequest(
    UUID userId,
    String aadharNumber,
    String panNumber
) { }
