package com.myfarmproduce.web.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Authenticated principal for both admins and customers - role decides which. */
public class AppPrincipal implements UserDetails {

    private final Integer id;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final String role; // "ADMIN" or "CUSTOMER"
    private final boolean mustChangePassword;

    public AppPrincipal(Integer id, String name, String email, String passwordHash, String role,
                         boolean mustChangePassword) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.mustChangePassword = mustChangePassword;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public boolean isMustChangePassword() { return mustChangePassword; }
    public boolean isAdmin() { return "ADMIN".equals(role); }
    public boolean isCustomer() { return "CUSTOMER".equals(role); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public String getUsername() { return email; }
}
