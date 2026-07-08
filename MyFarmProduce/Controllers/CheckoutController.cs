using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Application.Models;
using MyFarmProduce.Web.Extensions;
using MyFarmProduce.Web.Models;
using MyFarmProduce.Web.Services;

namespace MyFarmProduce.Web.Controllers;

// User Story 4: checkout — capture delivery details, show totals, create Pending order.
[Authorize(Roles = MyFarmProduce.Common.AppConstants.Roles.Customer)]
public class CheckoutController : Controller
{
    private readonly CartFactory _factory;
    private readonly ICartService _cart;
    private readonly IOrderService _orders;
    private readonly INotificationService _notifications;

    public CheckoutController(CartFactory factory, ICartService cart, IOrderService orders, INotificationService notifications)
    {
        _factory = factory;
        _cart = cart;
        _orders = orders;
        _notifications = notifications;
    }

    [HttpGet]
    public async Task<IActionResult> Index()
    {
        var cart = await _factory.BuildAsync();
        if (cart.IsEmpty)
        {
            TempData["Message"] = "Your cart is empty.";
            return RedirectToAction("Index", "Catalog");
        }

        return View(new CheckoutViewModel
        {
            Subtotal = cart.Subtotal,
            DeliveryFee = cart.DeliveryFee
        });
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Index(CheckoutViewModel model)
    {
        var cart = await _factory.BuildAsync();
        if (cart.IsEmpty)
            return RedirectToAction("Index", "Catalog");

        model.Subtotal = cart.Subtotal;
        model.DeliveryFee = cart.DeliveryFee;

        if (!ModelState.IsValid)
            return View(model);

        if (cart.HasStockIssues)
        {
            ModelState.AddModelError(string.Empty, "Some items exceed available stock. Please adjust your cart.");
            return View(model);
        }

        var lines = cart.Lines.Select(l => new CartLineInput(l.Product.Id, l.Quantity)).ToList();
        var request = new PlaceOrderRequest
        {
            DeliveryAddress = model.DeliveryAddress,
            Phone = model.Phone,
            DeliveryNote = model.DeliveryNote
        };

        try
        {
            var order = await _orders.CreateOrderAsync(User.GetCustomerId(), request, lines);
            _cart.Clear();

            var placed = await _orders.GetOrderAsync(order.Id);
            if (placed is not null) await _notifications.OrderPlacedAsync(placed);

            return RedirectToAction("Pay", "Payment", new { id = order.Id });
        }
        catch (InvalidOperationException ex)
        {
            ModelState.AddModelError(string.Empty, ex.Message);
            return View(model);
        }
    }
}
