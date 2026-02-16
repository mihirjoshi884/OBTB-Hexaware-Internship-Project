package org.hexaware.busservice.dtos;

import java.util.UUID;

public record CompanyCreationRequest(
        String companyName,
        String ownerName,
        UUID ownerId
) { }
