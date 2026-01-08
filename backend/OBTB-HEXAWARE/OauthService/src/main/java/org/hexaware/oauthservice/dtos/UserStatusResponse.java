package org.hexaware.oauthservice.dtos;

import java.util.UUID;

public record UserStatusResponse(
        UUID userId,
        String username,
        boolean isActive,
        boolean isVerified,
        String message
) {}

