package org.hexaware.oauthservice.services;

import org.hexaware.oauthservice.entites.AuthIdentity;
import org.hexaware.oauthservice.entites.PrincipleUser;
import org.hexaware.oauthservice.repositories.AuthIdentityRepository;
import org.hexaware.oauthservice.repositories.UserLockOutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AuthIdentityRepository authIdentityRepository;

    @Autowired
    private UserLockOutRepository userLockOutRepository;

    @Autowired
    private UserRoleResolveService roleResolveService;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        AuthIdentity authIdentity = authIdentityRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        boolean isNonLocked = userLockOutRepository
                .findByUserId(authIdentity.getUserId())
                .map(lockout ->
                        !(lockout.getLoginCounter() >= 3 && lockout.isLocked()))
                .orElse(true);

        // ✅ Resolve roles safely here
        List<? extends GrantedAuthority> authorities =
                roleResolveService.resolveRoles(authIdentity.getRoleMappingId())
                        .stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();

        return new PrincipleUser(authIdentity, isNonLocked, authorities);
    }
}
