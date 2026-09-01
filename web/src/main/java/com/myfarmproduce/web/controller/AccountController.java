package com.myfarmproduce.web.controller;

import com.myfarmproduce.application.service.AuthService;
import com.myfarmproduce.domain.entity.Customer;
import com.myfarmproduce.web.dto.ChangePasswordForm;
import com.myfarmproduce.web.dto.RegisterForm;
import com.myfarmproduce.web.security.AppPrincipal;
import com.myfarmproduce.web.security.AuthSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final AuthService authService;
    private final AuthSessionService authSession;

    public AccountController(AuthService authService, AuthSessionService authSession) {
        this.authService = authService;
        this.authSession = authSession;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "account/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form, BindingResult binding,
                            HttpServletRequest request, HttpServletResponse response) {
        if (!form.getPassword().equals(form.getConfirmPassword()))
            binding.rejectValue("confirmPassword", "mismatch", "Passwords do not match.");
        if (binding.hasErrors()) return "account/register";

        var customer = authService.register(form.getName(), form.getEmail(), form.getPhone(), form.getPassword());
        if (customer.isEmpty()) {
            binding.rejectValue("email", "duplicate", "That email is already registered.");
            return "account/register";
        }

        authSession.signIn(customer.get(), request, response);
        return "redirect:/catalog";
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String returnUrl,
                             @RequestParam(required = false) String error, Model model) {
        model.addAttribute("returnUrl", returnUrl);
        model.addAttribute("error", error != null);
        return "account/login";
    }

    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        model.addAttribute("form", new ChangePasswordForm());
        return "account/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("form") ChangePasswordForm form, BindingResult binding,
                                  @AuthenticationPrincipal AppPrincipal principal,
                                  HttpServletRequest request, HttpServletResponse response,
                                  RedirectAttributes redirectAttributes) {
        if (!form.getNewPassword().equals(form.getConfirmPassword()))
            binding.rejectValue("confirmPassword", "mismatch", "Passwords do not match.");
        if (binding.hasErrors()) return "account/change-password";

        authService.changePassword(principal.getId(), form.getNewPassword());

        // Re-issue the session principal without the must-change-password flag.
        var refreshed = authService.validateCredentials(principal.getUsername(), form.getNewPassword());
        refreshed.ifPresent(c -> authSession.signIn(c, request, response));

        redirectAttributes.addFlashAttribute("message", "Password changed. Welcome!");
        return "redirect:/catalog";
    }
}
