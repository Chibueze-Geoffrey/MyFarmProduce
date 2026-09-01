package com.myfarmproduce.web.controller;

import com.myfarmproduce.application.service.NotificationService;
import com.myfarmproduce.application.service.OrderService;
import com.myfarmproduce.application.service.PaymentGateway;
import com.myfarmproduce.domain.enums.OrderStatus;
import com.myfarmproduce.web.security.AppPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private static final String PROVIDER_NAME = "DevSimulated";

    private final OrderService orders;
    private final PaymentGateway gateway;
    private final NotificationService notifications;

    public PaymentController(OrderService orders, PaymentGateway gateway, NotificationService notifications) {
        this.orders = orders;
        this.gateway = gateway;
        this.notifications = notifications;
    }

    // Kick off payment: create a gateway transaction and redirect the customer to it.
    @GetMapping("/pay/{id}")
    public String pay(@PathVariable Integer id, @AuthenticationPrincipal AppPrincipal principal,
                       HttpServletRequest request, HttpServletResponse response) {
        var order = orders.getOrder(id, principal.getId());
        if (order.isEmpty()) { response.setStatus(404); return "error/404"; }
        if (order.get().getStatus() != OrderStatus.Pending)
            return "redirect:/payment/confirmation/" + order.get().getId();

        String callbackUrl = ServletUriComponentsBuilder.fromContextPath(request).path("/payment/simulate").toUriString();
        var init = gateway.initialize(order.get(), callbackUrl);
        orders.initiatePayment(order.get().getId(), PROVIDER_NAME, init.reference());

        return "redirect:" + init.redirectUrl();
    }

    // Stand-in for the gateway-hosted payment page (dev only).
    @GetMapping("/simulate")
    public String simulate(@RequestParam String reference, Model model) {
        model.addAttribute("reference", reference);
        return "payment/simulate";
    }

    // Redirect-back / callback from the gateway.
    @PostMapping("/callback")
    public String callback(@RequestParam String reference, @RequestParam boolean success,
                            RedirectAttributes redirectAttributes) {
        if (!success) {
            redirectAttributes.addFlashAttribute("message", "Payment was not completed. You can retry from your orders.");
            return "redirect:/orders";
        }

        var verification = gateway.verify(reference);
        if (!verification.success()) {
            redirectAttributes.addFlashAttribute("message", "Payment could not be verified.");
            return "redirect:/orders";
        }

        var order = orders.confirmPayment(reference);
        if (order.isEmpty()) return "redirect:/orders";

        orders.getOrder(order.get().getId(), null).ifPresent(notifications::paymentConfirmed);

        return "redirect:/payment/confirmation/" + order.get().getId();
    }

    // Async webhook - the source of truth for confirmation (don't rely on redirect alone).
    @PostMapping("/webhook")
    @ResponseBody
    public ResponseEntity<Void> webhook(@RequestParam(required = false) String reference) {
        if (reference == null || reference.isEmpty()) return ResponseEntity.badRequest().build();

        var order = orders.confirmPayment(reference);
        if (order.isEmpty()) return ResponseEntity.notFound().build();

        log.info("Webhook confirmed payment {} for order {}", reference, order.get().getId());
        return ResponseEntity.ok().build();
    }

    // User Story 6: order confirmation page.
    @GetMapping("/confirmation/{id}")
    public String confirmation(@PathVariable Integer id, @AuthenticationPrincipal AppPrincipal principal,
                                Model model, HttpServletResponse response) {
        var order = orders.getOrder(id, principal.getId());
        if (order.isEmpty()) { response.setStatus(404); return "error/404"; }
        model.addAttribute("order", order.get());
        return "payment/confirmation";
    }
}
