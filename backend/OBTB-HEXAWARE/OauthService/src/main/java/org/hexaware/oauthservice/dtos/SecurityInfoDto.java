package org.hexaware.oauthservice.dtos;

import java.time.Instant;

public record SecurityInfoDto(
        Instant passwordLastUpdated,
        Instant lastlogin
) { }
