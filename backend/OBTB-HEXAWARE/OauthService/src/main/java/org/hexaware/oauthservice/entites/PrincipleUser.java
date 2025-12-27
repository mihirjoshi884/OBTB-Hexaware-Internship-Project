package org.hexaware.oauthservice.entites;

import org.hexaware.oauthservice.services.UserRoleResolveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


public class PrincipleUser implements UserDetails {

    private final AuthIdentity authIdentity;
    private final boolean accountNonLocked;
    private final Collection<? extends GrantedAuthority> authorities;

    public PrincipleUser(AuthIdentity authIdentity,
                         boolean accountNonLocked,
                         Collection<? extends GrantedAuthority> authorities) {
        this.authIdentity = authIdentity;
        this.accountNonLocked = accountNonLocked;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return authIdentity.getHashPassword();
    }

    @Override
    public String getUsername() {
        return authIdentity.getUsername();
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isEnabled() {
        return authIdentity.is_Active();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}
