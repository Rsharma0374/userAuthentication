package com.userAuthentication.model.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {


    private final UserRegistry userRegistry;

    public UserPrincipal(UserRegistry userRegistry) {
        this.userRegistry = userRegistry;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getPassword() {
        return userRegistry.getPassword();
    }

    @Override
    public String getUsername() {
        return userRegistry.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return userRegistry.isAccountActive();
    }

    @Override
    public boolean isAccountNonLocked() {
        return userRegistry.isAccountActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return userRegistry.isAccountActive();
    }

    @Override
    public boolean isEnabled() {
        return userRegistry.isAccountActive();
    }
}
