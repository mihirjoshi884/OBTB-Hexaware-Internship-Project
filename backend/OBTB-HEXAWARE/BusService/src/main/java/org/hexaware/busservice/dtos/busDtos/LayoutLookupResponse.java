package org.hexaware.busservice.dtos.busDtos;

import java.util.UUID;

public record LayoutLookupResponse(
        UUID layoutId,
        String layoutName,
        String description
)
{ }
