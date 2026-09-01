package com.myfarmproduce.web.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        AppPrincipal principal = (AppPrincipal) authentication.getPrincipal();

        String target;
        if (principal.isAdmin()) {
            target = "/admin/products";
        } else if (principal.isMustChangePassword()) {
            target = "/account/change-password";
        } else {
            target = resolveReturnUrl(request);
        }

        // The saved request (if any) is only meant to resume a *protected* page the
        // user was redirected away from - once we've decided a plain redirect target,
        // discard it so a stale saved request doesn't hijack a later navigation.
        requestCache.removeRequest(request, response);

        response.sendRedirect(request.getContextPath() + target);
    }

    private String resolveReturnUrl(HttpServletRequest request) {
        String returnUrl = request.getParameter("returnUrl");
        if (StringUtils.hasText(returnUrl) && returnUrl.startsWith("/") && !returnUrl.startsWith("//"))
            return returnUrl;

        SavedRequest saved = requestCache.getRequest(request, null);
        if (saved != null) {
            String savedUrl = saved.getRedirectUrl();
            String prefix = request.getRequestURL().toString().replace(request.getRequestURI(), "") + request.getContextPath();
            if (savedUrl.startsWith(prefix))
                return savedUrl.substring(prefix.length());
        }

        return "/catalog";
    }
}
