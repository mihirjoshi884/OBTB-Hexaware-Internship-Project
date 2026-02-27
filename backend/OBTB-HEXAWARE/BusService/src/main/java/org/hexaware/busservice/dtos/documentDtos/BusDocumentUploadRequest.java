package org.hexaware.busservice.dtos.documentDtos;

import java.util.UUID;

public record BusDocumentUploadRequest(
    UUID ownerId,
    UUID companyId,
    UUID busId
) { }
