package org.hexaware.busservice.dtos;

import java.util.UUID;

public record CompanyCreationResponse(
        UUID companyId,
        String companyName,
        String ownerName,
        UUID ownerId
) { }
