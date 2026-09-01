package com.myfarmproduce.web.controller;

import com.myfarmproduce.web.service.CartFactory;
import com.myfarmproduce.web.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cart;
    private final CartFactory factory;

    public CartController(CartService cart, CartFactory factory) {
        this.cart = cart;
        this.factory = factory;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("cart", factory.build());
        return "cart/index";
    }

    @PostMapping("/add")
    public Object add(@RequestParam Integer productId, @RequestParam(defaultValue = "1") int quantity,
                       @RequestParam(required = false) String returnUrl, RedirectAttributes redirectAttributes) {
        cart.add(productId, quantity);
        redirectAttributes.addFlashAttribute("message", "Added to cart.");
        if (StringUtils.hasText(returnUrl) && returnUrl.startsWith("/") && !returnUrl.startsWith("//"))
            return new RedirectView(returnUrl);
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String update(@RequestParam Integer productId, @RequestParam int quantity) {
        cart.setQuantity(productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam Integer productId) {
        cart.remove(productId);
        return "redirect:/cart";
    }
}
