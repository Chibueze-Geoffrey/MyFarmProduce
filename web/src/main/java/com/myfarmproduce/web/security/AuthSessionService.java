package com.myfarmproduce.web.security;

import com.myfarmproduce.domain.entity.Customer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

/** Programmatically signs a customer in (register) or refreshes the current session's principal (password change). */
@Component
public class AuthSessionService {

    private final SecurityContextRepository securityContextRepository;

    public AuthSessionService(SecurityContextRepository securityContextRepository) {
        this.securityContextRepository = securityContextRepository;
    }

    public void signIn(Customer customer, HttpServletRequest request, HttpServletResponse response) {
        AppPrincipal principal = new AppPrincipal(customer.getId(), customer.getName(), customer.getEmail(),
                customer.getPasswordHash(), "CUSTOMER", customer.isMustChangePassword());
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}
