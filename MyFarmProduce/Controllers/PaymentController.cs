using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Common;
using MyFarmProduce.Common.Enums;
using MyFarmProduce.Infrastructure.Services;
using MyFarmProduce.Web.Extensions;

namespace MyFarmProduce.Web.Controllers;

// User Story 5: payment via gateway abstraction, with callback + webhook confirmation.
public class PaymentController : Controller
{
    private readonly IOrderService _orders;
    private readonly IPaymentGateway _gateway;
    private readonly INotificationService _notifications;
    private readonly ILogger<PaymentController> _logger;

    public PaymentController(IOrderService orders, IPaymentGateway gateway,
        INotificationService notifications, ILogger<PaymentController> logger)
    {
        _orders = orders;
        _gateway = gateway;
        _notifications = notifications;
        _logger = logger;
    }

    // Kick off payment: create a gateway transaction and redirect the customer to it.
    [Authorize(Roles = AppConstants.Roles.Customer)]
    [HttpGet]
    public async Task<IActionResult> Pay(int id)
    {
        var order = await _orders.GetOrderAsync(id, User.GetCustomerId());
        if (order is null) return NotFound();
        if (order.Status != OrderStatus.Pending)
            return RedirectToAction(nameof(Confirmation), new { id = order.Id });

        var callbackUrl = Url.Action(nameof(Simulate), "Payment", null, Request.Scheme)!;
        var init = await _gateway.InitializeAsync(order, callbackUrl);
        await _orders.InitiatePaymentAsync(order.Id, DevPaymentGateway.ProviderName, init.Reference);

        return Redirect(init.RedirectUrl);
    }

    // Stand-in for the gateway-hosted payment page (dev only).
    [Authorize(Roles = AppConstants.Roles.Customer)]
    [HttpGet]
    public IActionResult Simulate(string reference)
    {
        ViewData["Reference"] = reference;
        return View();
    }

    // Redirect-back / callback from the gateway.
    [Authorize(Roles = AppConstants.Roles.Customer)]
    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Callback(string reference, bool success)
    {
        if (!success)
        {
            TempData["Message"] = "Payment was not completed. You can retry from your orders.";
            return RedirectToAction("Index", "Orders");
        }

        var verification = await _gateway.VerifyAsync(reference);
        if (!verification.Success)
        {
            TempData["Message"] = "Payment could not be verified.";
            return RedirectToAction("Index", "Orders");
        }

        var order = await _orders.ConfirmPaymentAsync(reference);
        if (order is null) return NotFound();

        var full = await _orders.GetOrderAsync(order.Id);
        if (full is not null) await _notifications.PaymentConfirmedAsync(full);

        return RedirectToAction(nameof(Confirmation), new { id = order.Id });
    }

    // Async webhook — the source of truth for confirmation (don't rely on redirect alone).
    [AllowAnonymous]
    [HttpPost, IgnoreAntiforgeryToken]
    public async Task<IActionResult> Webhook(string reference)
    {
        if (string.IsNullOrEmpty(reference)) return BadRequest();

        var order = await _orders.ConfirmPaymentAsync(reference);
        if (order is null) return NotFound();

        _logger.LogInformation("Webhook confirmed payment {Reference} for order {OrderId}", reference, order.Id);
        return Ok();
    }

    // User Story 6: order confirmation page.
    [Authorize(Roles = AppConstants.Roles.Customer)]
    [HttpGet]
    public async Task<IActionResult> Confirmation(int id)
    {
        var order = await _orders.GetOrderAsync(id, User.GetCustomerId());
        if (order is null) return NotFound();
        return View(order);
    }
}
