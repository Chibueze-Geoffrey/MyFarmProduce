package com.myfarmproduce.web.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleAwareLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                 Authentication authentication) throws IOException {
        boolean wasAdmin = authentication != null && authentication.getPrincipal() instanceof AppPrincipal ap && ap.isAdmin();
        response.sendRedirect(request.getContextPath() + (wasAdmin ? "/account/login" : "/catalog"));
    }
}
