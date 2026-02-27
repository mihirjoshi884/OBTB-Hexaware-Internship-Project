package org.hexaware.busservice.dtos.documentDtos;

import java.util.List;
import java.util.UUID;

public record BusDocumentUploadResponse (

        UUID busId,
        UUID ownerId,
        UUID companyId,
        BusDocumentResponse insurancePolicy,
        BusDocumentResponse rcBook,
        BusDocumentResponse registrationNumberPlate
){ }
