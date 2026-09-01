package com.myfarmproduce.web.controller;

import com.myfarmproduce.application.model.CartLineInput;
import com.myfarmproduce.application.model.PlaceOrderRequest;
import com.myfarmproduce.application.service.NotificationService;
import com.myfarmproduce.application.service.OrderService;
import com.myfarmproduce.web.dto.CheckoutForm;
import com.myfarmproduce.web.security.AppPrincipal;
import com.myfarmproduce.web.service.CartFactory;
import com.myfarmproduce.web.service.CartService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CartFactory cartFactory;
    private final CartService cart;
    private final OrderService orders;
    private final NotificationService notifications;

    public CheckoutController(CartFactory cartFactory, CartService cart, OrderService orders,
                               NotificationService notifications) {
        this.cartFactory = cartFactory;
        this.cart = cart;
        this.orders = orders;
        this.notifications = notifications;
    }

    @GetMapping
    public String index(Model model, RedirectAttributes redirectAttributes) {
        var cartVm = cartFactory.build();
        if (cartVm.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Your cart is empty.");
            return "redirect:/catalog";
        }

        CheckoutForm form = new CheckoutForm();
        form.setSubtotal(cartVm.getSubtotal());
        form.setDeliveryFee(cartVm.getDeliveryFee());
        model.addAttribute("form", form);
        return "checkout/index";
    }

    @PostMapping
    public String submit(@Valid @ModelAttribute("form") CheckoutForm form, BindingResult binding,
                          @AuthenticationPrincipal AppPrincipal principal, Model model) {
        var cartVm = cartFactory.build();
        if (cartVm.isEmpty()) return "redirect:/catalog";

        form.setSubtotal(cartVm.getSubtotal());
        form.setDeliveryFee(cartVm.getDeliveryFee());

        if (binding.hasErrors()) return "checkout/index";

        if (cartVm.isHasStockIssues()) {
            binding.reject("stock", "Some items exceed available stock. Please adjust your cart.");
            return "checkout/index";
        }

        List<CartLineInput> lines = cartVm.getLines().stream()
                .map(l -> new CartLineInput(l.getProduct().getId(), l.getQuantity()))
                .toList();

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setDeliveryAddress(form.getDeliveryAddress());
        request.setPhone(form.getPhone());
        request.setDeliveryNote(form.getDeliveryNote());

        try {
            var order = orders.createOrder(principal.getId(), request, lines);
            cart.clear();

            orders.getOrder(order.getId(), null).ifPresent(notifications::orderPlaced);

            return "redirect:/payment/pay/" + order.getId();
        } catch (IllegalStateException e) {
            binding.reject("order", e.getMessage());
            return "checkout/index";
        }
    }
}
