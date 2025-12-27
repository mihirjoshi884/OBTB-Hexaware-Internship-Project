package org.hexaware.oauthservice.dtos;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public record CurrentUserResponse(
        String username,
        boolean enabled,
        boolean accountNonLocked,
        Collection<? extends GrantedAuthority> authorities
) {}
