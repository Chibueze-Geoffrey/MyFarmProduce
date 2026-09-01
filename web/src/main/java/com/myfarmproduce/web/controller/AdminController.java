package com.myfarmproduce.web.controller;

import com.myfarmproduce.application.service.*;
import com.myfarmproduce.common.AppConstants;
import com.myfarmproduce.domain.enums.OrderStatus;
import com.myfarmproduce.web.dto.AdminUserForm;
import com.myfarmproduce.web.dto.ProductForm;
import com.myfarmproduce.web.security.AppPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/** Admin area: inventory, orders, user management, profile-change approvals, support tickets, admin profile. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final InventoryService inventory;
    private final OrderService orders;
    private final NotificationService notifications;
    private final UserAdminService users;
    private final ProfileService profiles;
    private final SupportService support;
    private final FileStorage files;

    public AdminController(InventoryService inventory, OrderService orders, NotificationService notifications,
                            UserAdminService users, ProfileService profiles, SupportService support,
                            FileStorage files) {
        this.inventory = inventory;
        this.orders = orders;
        this.notifications = notifications;
        this.users = users;
        this.profiles = profiles;
        this.support = support;
        this.files = files;
    }

    // ---------- Inventory ----------

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", inventory.getAll());
        return "admin/products";
    }

    @GetMapping("/products/create")
    public String createProductForm(Model model) {
        model.addAttribute("categories", inventory.getCategories());
        model.addAttribute("form", new ProductForm());
        return "admin/product-form";
    }

    @PostMapping("/products/create")
    public String createProduct(@Valid @ModelAttribute("form") ProductForm form, BindingResult binding,
                                 @RequestParam(required = false) MultipartFile imageFile,
                                 Model model, RedirectAttributes redirectAttributes) {
        if (binding.hasErrors()) {
            model.addAttribute("categories", inventory.getCategories());
            return "admin/product-form";
        }

        String imageUrl = saveUpload(imageFile, AppConstants.UploadFolders.PRODUCTS);
        if (imageUrl == null) imageUrl = form.getImageUrl();

        inventory.create(form.getName(), form.getDescription(), form.getCategoryId(), form.getUnit(),
                form.getPrice(), form.getStockQty(), form.isAvailable(), imageUrl);
        redirectAttributes.addFlashAttribute("message", "Product created.");
        return "redirect:/admin/products";
    }

    @GetMapping("/products/{id}/edit")
    public String editProductForm(@PathVariable Integer id, Model model, HttpServletResponse response) {
        var product = inventory.getById(id);
        if (product == null) { response.setStatus(404); return "error/404"; }

        ProductForm form = new ProductForm();
        form.setId(product.getId());
        form.setName(product.getName());
        form.setDescription(product.getDescription());
        form.setCategoryId(product.getCategory().getId());
        form.setUnit(product.getUnit());
        form.setPrice(product.getPrice());
        form.setStockQty(product.getStockQty());
        form.setAvailable(product.isAvailable());
        form.setImageUrl(product.getImageUrl());

        model.addAttribute("categories", inventory.getCategories());
        model.addAttribute("form", form);
        return "admin/product-form";
    }

    @PostMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Integer id, @Valid @ModelAttribute("form") ProductForm form,
                               BindingResult binding, @RequestParam(required = false) MultipartFile imageFile,
                               Model model, RedirectAttributes redirectAttributes) {
        if (binding.hasErrors()) {
            model.addAttribute("categories", inventory.getCategories());
            return "admin/product-form";
        }

        String imageUrl = saveUpload(imageFile, AppConstants.UploadFolders.PRODUCTS);
        if (imageUrl == null) imageUrl = form.getImageUrl();

        inventory.update(id, form.getName(), form.getDescription(), form.getCategoryId(), form.getUnit(),
                form.getPrice(), form.isAvailable(), imageUrl);
        redirectAttributes.addFlashAttribute("message", "Product updated.");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/restock")
    public String restock(@PathVariable Integer id, @RequestParam int quantity, RedirectAttributes redirectAttributes) {
        try {
            inventory.restock(id, quantity);
            redirectAttributes.addFlashAttribute("message", "Restocked by " + quantity + ".");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", "Restock quantity must be greater than zero.");
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        inventory.delete(id);
        redirectAttributes.addFlashAttribute("message", "Product deleted.");
        return "redirect:/admin/products";
    }

    // ---------- Orders ----------

    @GetMapping("/orders")
    public String orders(@RequestParam(required = false) OrderStatus status,
                          @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate from,
                          @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate to,
                          Model model) {
        Instant fromInstant = from == null ? null : from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = to == null ? null : to.atStartOfDay(ZoneOffset.UTC).toInstant();

        model.addAttribute("orders", orders.getOrders(status, fromInstant, toInstant));
        model.addAttribute("status", status);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "admin/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetails(@PathVariable Integer id, Model model, HttpServletResponse response) {
        var order = orders.getOrder(id, null);
        if (order.isEmpty()) { response.setStatus(404); return "error/404"; }
        model.addAttribute("order", order.get());
        return "admin/order-details";
    }

    @PostMapping("/orders/{id}/status")
    public String updateStatus(@PathVariable Integer id, @RequestParam OrderStatus status,
                                RedirectAttributes redirectAttributes) {
        orders.updateStatus(id, status);

        orders.getOrder(id, null).ifPresent(o -> {
            if (status == OrderStatus.OutForDelivery) notifications.outForDelivery(o);
            else if (status == OrderStatus.Delivered) notifications.delivered(o);
        });

        redirectAttributes.addFlashAttribute("message", "Order #" + id + " set to " + status + ".");
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        orders.cancelOrder(id);
        redirectAttributes.addFlashAttribute("message", "Order #" + id + " cancelled.");
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/orders/{id}/refund")
    public String refundOrder(@PathVariable Integer id, @RequestParam(required = false) String note,
                               RedirectAttributes redirectAttributes) {
        orders.refundOrder(id, note == null ? "" : note);
        redirectAttributes.addFlashAttribute("message", "Order #" + id + " refunded (logged).");
        return "redirect:/admin/orders/" + id;
    }

    // ---------- User management ----------

    @GetMapping("/users")
    public String users(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("users", users.getCustomers(q));
        model.addAttribute("search", q);
        return "admin/users";
    }

    @GetMapping("/users/create")
    public String createUserForm(Model model) {
        model.addAttribute("form", new AdminUserForm());
        return "admin/user-form";
    }

    @PostMapping("/users/create")
    public String createUser(@Valid @ModelAttribute("form") AdminUserForm form, BindingResult binding,
                              RedirectAttributes redirectAttributes) {
        if (binding.hasErrors()) return "admin/user-form";

        var created = users.createCustomer(form.getName(), form.getEmail(), form.getPhone());
        if (created.isEmpty()) {
            binding.rejectValue("email", "duplicate", "That email is already registered.");
            return "admin/user-form";
        }
        redirectAttributes.addFlashAttribute("message", "User created. Default password: "
                + AppConstants.DEFAULT_USER_PASSWORD + " (they'll be prompted to change it on first login).");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Integer id, Model model, HttpServletResponse response) {
        var c = users.getCustomer(id);
        if (c.isEmpty()) { response.setStatus(404); return "error/404"; }

        AdminUserForm form = new AdminUserForm();
        form.setId(c.get().getId());
        form.setName(c.get().getName());
        form.setEmail(c.get().getEmail());
        form.setPhone(c.get().getPhone());
        model.addAttribute("form", form);
        return "admin/user-form";
    }

    @PostMapping("/users/{id}/edit")
    public String editUser(@PathVariable Integer id, @Valid @ModelAttribute("form") AdminUserForm form,
                            BindingResult binding, RedirectAttributes redirectAttributes) {
        if (binding.hasErrors()) return "admin/user-form";
        users.updateCustomer(id, form.getName(), form.getEmail(), form.getPhone());
        redirectAttributes.addFlashAttribute("message", "User updated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        users.deleteCustomer(id);
        redirectAttributes.addFlashAttribute("message", "User deleted.");
        return "redirect:/admin/users";
    }

    // ---------- Profile change requests ----------

    @GetMapping("/change-requests")
    public String changeRequests(Model model) {
        model.addAttribute("requests", users.getChangeRequests(false));
        return "admin/change-requests";
    }

    @PostMapping("/change-requests/{id}/approve")
    public String approveChange(@PathVariable Integer id, @RequestParam(required = false) String note,
                                 RedirectAttributes redirectAttributes) {
        users.approveChangeRequest(id, note);
        redirectAttributes.addFlashAttribute("message", "Change applied.");
        return "redirect:/admin/change-requests";
    }

    @PostMapping("/change-requests/{id}/reject")
    public String rejectChange(@PathVariable Integer id, @RequestParam(required = false) String note,
                                RedirectAttributes redirectAttributes) {
        users.rejectChangeRequest(id, note);
        redirectAttributes.addFlashAttribute("message", "Change rejected.");
        return "redirect:/admin/change-requests";
    }

    // ---------- Support tickets ----------

    @GetMapping("/support")
    public String support(Model model) {
        model.addAttribute("tickets", support.getAllTickets());
        return "admin/support";
    }

    @GetMapping("/support/{id}")
    public String ticket(@PathVariable Integer id, Model model, HttpServletResponse response) {
        var ticket = support.getTicket(id, null);
        if (ticket.isEmpty()) { response.setStatus(404); return "error/404"; }
        model.addAttribute("ticket", ticket.get());
        return "admin/ticket";
    }

    // ---------- Admin profile ----------

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal AppPrincipal principal, Model model, HttpServletResponse response) {
        var admin = profiles.getAdmin(principal.getId());
        if (admin.isEmpty()) { response.setStatus(404); return "error/404"; }
        model.addAttribute("admin", admin.get());
        return "admin/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String name, @RequestParam(required = false) MultipartFile photoFile,
                                 @AuthenticationPrincipal AppPrincipal principal, RedirectAttributes redirectAttributes) {
        String photoUrl = saveUpload(photoFile, AppConstants.UploadFolders.AVATARS);
        profiles.updateAdminProfile(principal.getId(), name, photoUrl);
        redirectAttributes.addFlashAttribute("message", "Profile updated.");
        return "redirect:/admin/profile";
    }

    // ---------- helpers ----------

    private String saveUpload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) return null;
        try {
            return files.saveImage(file.getInputStream(), file.getOriginalFilename(), folder);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
