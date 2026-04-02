package org.hexaware.oauthservice.dtos;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.UUID;

public record CurrentUserResponse(
        UUID userId,
        String username,
        boolean enabled,
        boolean accountNonLocked,
        java.util.List<SimpleGrantedAuthority> authorities
) {}
