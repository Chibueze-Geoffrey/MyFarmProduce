package com.myfarmproduce.web.controller;

import com.myfarmproduce.application.service.CatalogService;
import com.myfarmproduce.application.service.OrderService;
import com.myfarmproduce.web.security.AppPrincipal;
import com.myfarmproduce.web.service.CartService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class OrdersController {

    private final OrderService orders;
    private final CatalogService catalog;
    private final CartService cart;

    public OrdersController(OrderService orders, CatalogService catalog, CartService cart) {
        this.orders = orders;
        this.catalog = catalog;
        this.cart = cart;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal AppPrincipal principal, Model model) {
        model.addAttribute("orders", orders.getCustomerOrders(principal.getId()));
        return "orders/index";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Integer id, @AuthenticationPrincipal AppPrincipal principal,
                           Model model, HttpServletResponse response) {
        var order = orders.getOrder(id, principal.getId());
        if (order.isEmpty()) { response.setStatus(404); return "error/404"; }
        model.addAttribute("order", order.get());
        return "orders/details";
    }

    // Reorder: repopulate cart, adjusting for current stock/price/availability.
    @PostMapping("/{id}/reorder")
    public String reorder(@PathVariable Integer id, @AuthenticationPrincipal AppPrincipal principal,
                           RedirectAttributes redirectAttributes) {
        var order = orders.getOrder(id, principal.getId());
        if (order.isEmpty()) return "redirect:/orders";

        cart.clear();
        boolean adjusted = false;
        for (var item : order.get().getItems()) {
            var product = catalog.getProductById(item.getProduct().getId());
            if (product.isEmpty() || !product.get().isAvailable() || product.get().getStockQty() <= 0) {
                adjusted = true;
                continue;
            }
            int qty = Math.min(item.getQuantity(), product.get().getStockQty());
            if (qty != item.getQuantity()) adjusted = true;
            cart.add(product.get().getId(), qty);
        }

        redirectAttributes.addFlashAttribute("message", adjusted
                ? "Cart populated. Some items were adjusted for current stock/availability."
                : "Cart populated from your previous order.");
        return "redirect:/cart";
    }
}
