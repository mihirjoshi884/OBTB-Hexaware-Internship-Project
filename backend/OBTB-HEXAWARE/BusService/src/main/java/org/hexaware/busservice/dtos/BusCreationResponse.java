package org.hexaware.busservice.dtos;

import java.util.UUID;

public record BusCreationResponse(
        UUID busId,
        String busName,
        String companyName,
        String templateName
) { }
