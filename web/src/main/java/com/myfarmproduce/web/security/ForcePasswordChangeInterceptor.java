package com.myfarmproduce.web.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Forces a customer carrying mustChangePassword onto the change-password screen -
 * every other action redirects there until they set a new password.
 */
public class ForcePasswordChangeInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth != null && auth.getPrincipal() instanceof AppPrincipal principal) || !principal.isMustChangePassword())
            return true;

        String path = request.getRequestURI().substring(request.getContextPath().length());
        boolean allowed = path.equals("/account/change-password") || path.equals("/account/logout");
        if (allowed) return true;

        response.sendRedirect(request.getContextPath() + "/account/change-password");
        return false;
    }
}
