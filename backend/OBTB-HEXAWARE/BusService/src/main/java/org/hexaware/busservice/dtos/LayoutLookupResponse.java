package org.hexaware.busservice.dtos;

import java.util.UUID;

public record LayoutLookupResponse(
        UUID layoutId,
        String layoutName,
        String description
)
{ }
