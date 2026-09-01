package com.myfarmproduce.web.controller;

import com.myfarmproduce.application.service.FileStorage;
import com.myfarmproduce.application.service.ProfileService;
import com.myfarmproduce.common.AppConstants;
import com.myfarmproduce.web.security.AppPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.UncheckedIOException;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profiles;
    private final FileStorage files;

    public ProfileController(ProfileService profiles, FileStorage files) {
        this.profiles = profiles;
        this.files = files;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal AppPrincipal principal, Model model, HttpServletResponse response) {
        var customer = profiles.getCustomer(principal.getId());
        if (customer.isEmpty()) { response.setStatus(404); return "error/404"; }

        model.addAttribute("customer", customer.get());
        model.addAttribute("changeRequests", profiles.getMyChangeRequests(customer.get().getId()));
        return "profile/index";
    }

    @PostMapping
    public String update(@RequestParam String name, @RequestParam(required = false) MultipartFile photoFile,
                          @AuthenticationPrincipal AppPrincipal principal, RedirectAttributes redirectAttributes) {
        String photoUrl = saveUpload(photoFile, AppConstants.UploadFolders.AVATARS);
        profiles.updateCustomerProfile(principal.getId(), name, photoUrl);
        redirectAttributes.addFlashAttribute("message", "Profile updated.");
        return "redirect:/profile";
    }

    @PostMapping("/request-change")
    public String requestChange(@RequestParam String field, @RequestParam String requestedValue,
                                 @AuthenticationPrincipal AppPrincipal principal, RedirectAttributes redirectAttributes) {
        if (!("Phone".equals(field) || "Email".equals(field)) || !StringUtils.hasText(requestedValue)) {
            redirectAttributes.addFlashAttribute("message", "Please provide a valid value.");
            return "redirect:/profile";
        }

        profiles.requestFieldChange(principal.getId(), field, requestedValue);
        redirectAttributes.addFlashAttribute("message",
                "Request to change your " + field.toLowerCase() + " was submitted for admin approval.");
        return "redirect:/profile";
    }

    private String saveUpload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) return null;
        try {
            return files.saveImage(file.getInputStream(), file.getOriginalFilename(), folder);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
