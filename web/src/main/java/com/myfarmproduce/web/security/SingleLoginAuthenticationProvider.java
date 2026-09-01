package com.myfarmproduce.web.security;

import com.myfarmproduce.application.service.AdminAuthService;
import com.myfarmproduce.application.service.AuthService;
import com.myfarmproduce.domain.entity.Admin;
import com.myfarmproduce.domain.entity.Customer;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * Single login form for everyone: resolves the account type by checking the
 * Admin table first, then the Customer table - same behavior as the previous
 * AccountController.Login action.
 */
@Component
public class SingleLoginAuthenticationProvider implements AuthenticationProvider {

    private final AdminAuthService adminAuth;
    private final AuthService authService;

    public SingleLoginAuthenticationProvider(AdminAuthService adminAuth, AuthService authService) {
        this.adminAuth = adminAuth;
        this.authService = authService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());

        var admin = adminAuth.validateCredentials(email, password);
        if (admin.isPresent()) {
            Admin a = admin.get();
            AppPrincipal principal = new AppPrincipal(a.getId(), a.getName(), a.getEmail(), a.getPasswordHash(), "ADMIN", false);
            return new UsernamePasswordAuthenticationToken(principal, password, principal.getAuthorities());
        }

        var customer = authService.validateCredentials(email, password);
        if (customer.isPresent()) {
            Customer c = customer.get();
            AppPrincipal principal = new AppPrincipal(c.getId(), c.getName(), c.getEmail(), c.getPasswordHash(),
                    "CUSTOMER", c.isMustChangePassword());
            return new UsernamePasswordAuthenticationToken(principal, password, principal.getAuthorities());
        }

        throw new BadCredentialsException("Invalid email or password.");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
