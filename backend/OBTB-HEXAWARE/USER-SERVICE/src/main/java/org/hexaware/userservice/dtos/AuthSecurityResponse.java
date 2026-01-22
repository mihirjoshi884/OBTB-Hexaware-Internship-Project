package org.hexaware.userservice.dtos;

import java.time.Instant;

public record AuthSecurityResponse(
        Instant passwordLastUpdated,
        Instant lastlogin
) { }
