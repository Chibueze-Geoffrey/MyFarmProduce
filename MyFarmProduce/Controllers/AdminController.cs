using System.Security.Claims;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Common;
using MyFarmProduce.Common.Enums;
using MyFarmProduce.Domain.Entities;
using MyFarmProduce.Web.Extensions;
using MyFarmProduce.Web.Models;

namespace MyFarmProduce.Web.Controllers;

// Admin area: separate account type. Inventory (US9), orders (US10), user management,
// profile-change approvals, support tickets, and admin profile.
[Authorize(Roles = AppConstants.Roles.Admin)]
public class AdminController : Controller
{
    private readonly IInventoryService _inventory;
    private readonly IOrderService _orders;
    private readonly INotificationService _notifications;
    private readonly IAdminAuthService _adminAuth;
    private readonly IUserAdminService _users;
    private readonly IProfileService _profiles;
    private readonly ISupportService _support;
    private readonly IFileStorage _files;

    public AdminController(
        IInventoryService inventory, IOrderService orders, INotificationService notifications,
        IAdminAuthService adminAuth, IUserAdminService users, IProfileService profiles,
        ISupportService support, IFileStorage files)
    {
        _inventory = inventory;
        _orders = orders;
        _notifications = notifications;
        _adminAuth = adminAuth;
        _users = users;
        _profiles = profiles;
        _support = support;
        _files = files;
    }

    // ---------- Admin authentication ----------
    // There is a single login form for everyone (/Account/Login) that resolves the
    // account type by role. Any old /Admin/Login link just forwards there.

    [AllowAnonymous, HttpGet]
    public IActionResult Login(string? returnUrl) =>
        RedirectToAction("Login", "Account", new { returnUrl });

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Logout()
    {
        await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);
        return RedirectToAction("Login", "Account");
    }

    // ---------- Inventory ----------

    public async Task<IActionResult> Products()
    {
        return View(await _inventory.GetAllAsync());
    }

    [HttpGet]
    public async Task<IActionResult> CreateProduct()
    {
        await PopulateCategories();
        return View("ProductForm", new ProductFormViewModel());
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> CreateProduct(ProductFormViewModel model, IFormFile? imageFile)
    {
        if (!ModelState.IsValid)
        {
            await PopulateCategories();
            return View("ProductForm", model);
        }

        var imageUrl = await SaveUploadAsync(imageFile, AppConstants.UploadFolders.Products) ?? model.ImageUrl;

        await _inventory.CreateAsync(model.Name, model.Description, model.CategoryId, model.Unit,
            model.Price, model.StockQty, model.IsAvailable, imageUrl);
        TempData["Message"] = "Product created.";
        return RedirectToAction(nameof(Products));
    }

    [HttpGet]
    public async Task<IActionResult> EditProduct(int id)
    {
        var product = await _inventory.GetByIdAsync(id);
        if (product is null) return NotFound();

        await PopulateCategories();
        return View("ProductForm", new ProductFormViewModel
        {
            Id = product.Id,
            Name = product.Name,
            Description = product.Description,
            CategoryId = product.CategoryId,
            Unit = product.Unit,
            Price = product.Price,
            StockQty = product.StockQty,
            IsAvailable = product.IsAvailable,
            ImageUrl = product.ImageUrl
        });
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> EditProduct(ProductFormViewModel model, IFormFile? imageFile)
    {
        if (!ModelState.IsValid)
        {
            await PopulateCategories();
            return View("ProductForm", model);
        }

        var imageUrl = await SaveUploadAsync(imageFile, AppConstants.UploadFolders.Products) ?? model.ImageUrl;

        await _inventory.UpdateAsync(model.Id, model.Name, model.Description, model.CategoryId,
            model.Unit, model.Price, model.IsAvailable, imageUrl);
        TempData["Message"] = "Product updated.";
        return RedirectToAction(nameof(Products));
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Restock(int id, int quantity)
    {
        try
        {
            await _inventory.RestockAsync(id, quantity);
            TempData["Message"] = $"Restocked by {quantity}.";
        }
        catch (ArgumentOutOfRangeException)
        {
            TempData["Message"] = "Restock quantity must be greater than zero.";
        }
        return RedirectToAction(nameof(Products));
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> DeleteProduct(int id)
    {
        await _inventory.DeleteAsync(id);
        TempData["Message"] = "Product deleted.";
        return RedirectToAction(nameof(Products));
    }

    // ---------- Orders ----------

    public async Task<IActionResult> Orders(OrderStatus? status, DateTime? from, DateTime? to)
    {
        ViewData["Status"] = status;
        ViewData["From"] = from?.ToString("yyyy-MM-dd");
        ViewData["To"] = to?.ToString("yyyy-MM-dd");
        return View(await _orders.GetOrdersAsync(status, from, to));
    }

    public async Task<IActionResult> OrderDetails(int id)
    {
        var order = await _orders.GetOrderAsync(id);
        if (order is null) return NotFound();
        return View(order);
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> UpdateStatus(int id, OrderStatus status)
    {
        await _orders.UpdateStatusAsync(id, status);

        var order = await _orders.GetOrderAsync(id);
        if (order is not null)
        {
            if (status == OrderStatus.OutForDelivery) await _notifications.OutForDeliveryAsync(order);
            else if (status == OrderStatus.Delivered) await _notifications.DeliveredAsync(order);
        }

        TempData["Message"] = $"Order #{id} set to {status}.";
        return RedirectToAction(nameof(OrderDetails), new { id });
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> CancelOrder(int id)
    {
        await _orders.CancelOrderAsync(id);
        TempData["Message"] = $"Order #{id} cancelled.";
        return RedirectToAction(nameof(OrderDetails), new { id });
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> RefundOrder(int id, string note)
    {
        await _orders.RefundOrderAsync(id, note ?? string.Empty);
        TempData["Message"] = $"Order #{id} refunded (logged).";
        return RedirectToAction(nameof(OrderDetails), new { id });
    }

    // ---------- User management ----------

    public async Task<IActionResult> Users(string? q)
    {
        ViewData["Search"] = q;
        return View(await _users.GetCustomersAsync(q));
    }

    [HttpGet]
    public IActionResult CreateUser() => View("UserForm", new AdminUserFormViewModel());

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> CreateUser(AdminUserFormViewModel model)
    {
        if (!ModelState.IsValid) return View("UserForm", model);

        var created = await _users.CreateCustomerAsync(model.Name, model.Email, model.Phone);
        if (created is null)
        {
            ModelState.AddModelError(nameof(model.Email), "That email is already registered.");
            return View("UserForm", model);
        }
        TempData["Message"] = $"User created. Default password: {AppConstants.DefaultUserPassword} (they'll be prompted to change it on first login).";
        return RedirectToAction(nameof(Users));
    }

    [HttpGet]
    public async Task<IActionResult> EditUser(int id)
    {
        var c = await _users.GetCustomerAsync(id);
        if (c is null) return NotFound();
        return View("UserForm", new AdminUserFormViewModel { Id = c.Id, Name = c.Name, Email = c.Email, Phone = c.Phone });
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> EditUser(AdminUserFormViewModel model)
    {
        if (!ModelState.IsValid) return View("UserForm", model);
        await _users.UpdateCustomerAsync(model.Id, model.Name, model.Email, model.Phone);
        TempData["Message"] = "User updated.";
        return RedirectToAction(nameof(Users));
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> DeleteUser(int id)
    {
        await _users.DeleteCustomerAsync(id);
        TempData["Message"] = "User deleted.";
        return RedirectToAction(nameof(Users));
    }

    // ---------- Profile change requests ----------

    public async Task<IActionResult> ChangeRequests()
    {
        return View(await _users.GetChangeRequestsAsync(pendingOnly: false));
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> ApproveChange(int id, string? note)
    {
        await _users.ApproveChangeRequestAsync(id, note);
        TempData["Message"] = "Change applied.";
        return RedirectToAction(nameof(ChangeRequests));
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> RejectChange(int id, string? note)
    {
        await _users.RejectChangeRequestAsync(id, note);
        TempData["Message"] = "Change rejected.";
        return RedirectToAction(nameof(ChangeRequests));
    }

    // ---------- Support tickets ----------

    public async Task<IActionResult> Support()
    {
        return View(await _support.GetAllTicketsAsync());
    }

    public async Task<IActionResult> Ticket(int id)
    {
        var ticket = await _support.GetTicketAsync(id);
        if (ticket is null) return NotFound();
        return View(ticket);
    }

    // ---------- Admin profile ----------

    [HttpGet]
    public async Task<IActionResult> Profile()
    {
        var admin = await _profiles.GetAdminAsync(User.GetCustomerId());
        if (admin is null) return NotFound();
        return View(admin);
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Profile(string name, IFormFile? photoFile)
    {
        var photoUrl = await SaveUploadAsync(photoFile, AppConstants.UploadFolders.Avatars);
        await _profiles.UpdateAdminProfileAsync(User.GetCustomerId(), name, photoUrl);
        TempData["Message"] = "Profile updated.";
        return RedirectToAction(nameof(Profile));
    }

    // ---------- helpers ----------

    private async Task<string?> SaveUploadAsync(IFormFile? file, string folder)
    {
        if (file is null || file.Length == 0) return null;
        await using var stream = file.OpenReadStream();
        return await _files.SaveImageAsync(stream, file.FileName, folder);
    }

    private async Task PopulateCategories()
    {
        var categories = await _inventory.GetCategoriesAsync();
        ViewBag.Categories = new SelectList(categories, "Id", "Name");
    }
}
