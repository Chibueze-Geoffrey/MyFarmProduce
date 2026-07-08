using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Common;
using MyFarmProduce.Web.Extensions;
using MyFarmProduce.Web.Models;

namespace MyFarmProduce.Web.Controllers;

// Customer profile: edit name/photo; phone & email are locked (change via admin request).
[Authorize(Roles = AppConstants.Roles.Customer)]
public class ProfileController : Controller
{
    private readonly IProfileService _profiles;
    private readonly IFileStorage _files;

    public ProfileController(IProfileService profiles, IFileStorage files)
    {
        _profiles = profiles;
        _files = files;
    }

    [HttpGet]
    public async Task<IActionResult> Index()
    {
        var customer = await _profiles.GetCustomerAsync(User.GetCustomerId());
        if (customer is null) return NotFound();

        return View(new CustomerProfileViewModel
        {
            Customer = customer,
            Name = customer.Name,
            ChangeRequests = await _profiles.GetMyChangeRequestsAsync(customer.Id)
        });
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Index(string name, IFormFile? photoFile)
    {
        string? photoUrl = null;
        if (photoFile is { Length: > 0 })
        {
            await using var stream = photoFile.OpenReadStream();
            photoUrl = await _files.SaveImageAsync(stream, photoFile.FileName, AppConstants.UploadFolders.Avatars);
        }

        await _profiles.UpdateCustomerProfileAsync(User.GetCustomerId(), name, photoUrl);
        TempData["Message"] = "Profile updated.";
        return RedirectToAction(nameof(Index));
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> RequestChange(string field, string requestedValue)
    {
        if (field is not ("Phone" or "Email") || string.IsNullOrWhiteSpace(requestedValue))
        {
            TempData["Message"] = "Please provide a valid value.";
            return RedirectToAction(nameof(Index));
        }

        await _profiles.RequestFieldChangeAsync(User.GetCustomerId(), field, requestedValue);
        TempData["Message"] = $"Request to change your {field.ToLower()} was submitted for admin approval.";
        return RedirectToAction(nameof(Index));
    }
}
