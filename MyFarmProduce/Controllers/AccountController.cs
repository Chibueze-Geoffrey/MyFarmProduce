using System.Security.Claims;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Mvc;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Common;
using MyFarmProduce.Domain.Entities;
using MyFarmProduce.Web.Models;

namespace MyFarmProduce.Web.Controllers;

public class AccountController : Controller
{
    private readonly IAuthService _auth;
    private readonly IAdminAuthService _adminAuth;

    public AccountController(IAuthService auth, IAdminAuthService adminAuth)
    {
        _auth = auth;
        _adminAuth = adminAuth;
    }

    [HttpGet]
    public IActionResult Register() => View(new RegisterViewModel());

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Register(RegisterViewModel model)
    {
        if (!ModelState.IsValid) return View(model);

        var customer = await _auth.RegisterAsync(model.Name, model.Email, model.Phone, model.Password);
        if (customer is null)
        {
            ModelState.AddModelError(nameof(model.Email), "That email is already registered.");
            return View(model);
        }

        await SignInAsync(customer);
        return RedirectToAction("Index", "Catalog");
    }

    [HttpGet]
    public IActionResult Login(string? returnUrl) => View(new LoginViewModel { ReturnUrl = returnUrl });

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Login(LoginViewModel model)
    {
        if (!ModelState.IsValid) return View(model);

        // Single login form: resolve the account type by role. Admins are checked first.
        var admin = await _adminAuth.ValidateCredentialsAsync(model.Email, model.Password);
        if (admin is not null)
        {
            await SignInAdminAsync(admin);
            if (!string.IsNullOrEmpty(model.ReturnUrl) && Url.IsLocalUrl(model.ReturnUrl))
                return Redirect(model.ReturnUrl);
            return RedirectToAction("Products", "Admin");
        }

        var customer = await _auth.ValidateCredentialsAsync(model.Email, model.Password);
        if (customer is null)
        {
            ModelState.AddModelError(string.Empty, "Invalid email or password.");
            return View(model);
        }

        await SignInAsync(customer);

        if (customer.MustChangePassword)
            return RedirectToAction(nameof(ChangePassword));

        if (!string.IsNullOrEmpty(model.ReturnUrl) && Url.IsLocalUrl(model.ReturnUrl))
            return Redirect(model.ReturnUrl);
        return RedirectToAction("Index", "Catalog");
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Logout()
    {
        await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);
        return RedirectToAction("Index", "Catalog");
    }

    private async Task SignInAsync(Customer customer)
    {
        var claims = new List<Claim>
        {
            new(ClaimTypes.NameIdentifier, customer.Id.ToString()),
            new(ClaimTypes.Name, customer.Name),
            new(ClaimTypes.Email, customer.Email),
            new(ClaimTypes.Role, AppConstants.Roles.Customer)
        };
        if (customer.MustChangePassword)
            claims.Add(new Claim(AppConstants.Claims.MustChangePassword, "true"));

        var identity = new ClaimsIdentity(claims, CookieAuthenticationDefaults.AuthenticationScheme);
        await HttpContext.SignInAsync(CookieAuthenticationDefaults.AuthenticationScheme, new ClaimsPrincipal(identity));
    }

    [Microsoft.AspNetCore.Authorization.Authorize(Roles = AppConstants.Roles.Customer)]
    [HttpGet]
    public IActionResult ChangePassword() => View(new ChangePasswordViewModel());

    [Microsoft.AspNetCore.Authorization.Authorize(Roles = AppConstants.Roles.Customer)]
    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> ChangePassword(ChangePasswordViewModel model)
    {
        if (!ModelState.IsValid) return View(model);

        var customerId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        await _auth.ChangePasswordAsync(customerId, model.NewPassword);

        // Re-issue the cookie without the must-change claim.
        var customer = await _auth.ValidateCredentialsAsync(User.FindFirstValue(ClaimTypes.Email)!, model.NewPassword);
        if (customer is not null) await SignInAsync(customer);

        TempData["Message"] = "Password changed. Welcome!";
        return RedirectToAction("Index", "Catalog");
    }

    private async Task SignInAdminAsync(Admin admin)
    {
        var claims = new List<Claim>
        {
            new(ClaimTypes.NameIdentifier, admin.Id.ToString()),
            new(ClaimTypes.Name, admin.Name),
            new(ClaimTypes.Email, admin.Email),
            new(ClaimTypes.Role, AppConstants.Roles.Admin)
        };
        var identity = new ClaimsIdentity(claims, CookieAuthenticationDefaults.AuthenticationScheme);
        await HttpContext.SignInAsync(CookieAuthenticationDefaults.AuthenticationScheme, new ClaimsPrincipal(identity));
    }
}
