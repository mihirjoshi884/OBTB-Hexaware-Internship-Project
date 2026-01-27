package org.hexaware.busservice.dtos;

import java.util.UUID;

public record DocumentUploadRequest(
    UUID userId,
    String aadharNumber,
    String panNumber
) { }
