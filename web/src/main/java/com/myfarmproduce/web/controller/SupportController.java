package com.myfarmproduce.web.controller;

import com.myfarmproduce.application.service.SupportService;
import com.myfarmproduce.web.security.AppPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/support")
public class SupportController {

    private final SupportService support;

    public SupportController(SupportService support) {
        this.support = support;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal AppPrincipal principal, Model model) {
        model.addAttribute("tickets", support.getCustomerTickets(principal.getId()));
        return "support/index";
    }

    @PostMapping("/start")
    public String start(@RequestParam String message, @AuthenticationPrincipal AppPrincipal principal) {
        if (!StringUtils.hasText(message)) return "redirect:/support";
        var ticket = support.startTicket(principal.getId(), message);
        return "redirect:/support/" + ticket.getId();
    }

    @GetMapping("/{id}")
    public String ticket(@PathVariable Integer id, @AuthenticationPrincipal AppPrincipal principal,
                          Model model, HttpServletResponse response) {
        var ticket = support.getTicket(id, principal.getId());
        if (ticket.isEmpty()) { response.setStatus(404); return "error/404"; }
        model.addAttribute("ticket", ticket.get());
        return "support/ticket";
    }

    @PostMapping("/{id}/send")
    public String send(@PathVariable Integer id, @RequestParam String message,
                        @AuthenticationPrincipal AppPrincipal principal) {
        if (StringUtils.hasText(message)) support.sendCustomerMessage(id, principal.getId(), message);
        return "redirect:/support/" + id;
    }
}
