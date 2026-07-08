using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Web.Extensions;
using MyFarmProduce.Web.Services;

namespace MyFarmProduce.Web.Controllers;

// User Story 7 (track) + 8 (history/reorder).
[Authorize(Roles = MyFarmProduce.Common.AppConstants.Roles.Customer)]
public class OrdersController : Controller
{
    private readonly IOrderService _orders;
    private readonly ICatalogService _catalog;
    private readonly ICartService _cart;

    public OrdersController(IOrderService orders, ICatalogService catalog, ICartService cart)
    {
        _orders = orders;
        _catalog = catalog;
        _cart = cart;
    }

    public async Task<IActionResult> Index()
    {
        var orders = await _orders.GetCustomerOrdersAsync(User.GetCustomerId());
        return View(orders);
    }

    public async Task<IActionResult> Details(int id)
    {
        var order = await _orders.GetOrderAsync(id, User.GetCustomerId());
        if (order is null) return NotFound();
        return View(order);
    }

    // Reorder: repopulate cart, adjusting for current stock/price/availability.
    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Reorder(int id)
    {
        var order = await _orders.GetOrderAsync(id, User.GetCustomerId());
        if (order is null) return NotFound();

        _cart.Clear();
        var adjusted = false;
        foreach (var item in order.Items)
        {
            var product = await _catalog.GetProductByIdAsync(item.ProductId);
            if (product is null || !product.IsAvailable || product.StockQty <= 0)
            {
                adjusted = true;
                continue;
            }
            var qty = Math.Min(item.Quantity, product.StockQty);
            if (qty != item.Quantity) adjusted = true;
            _cart.Add(product.Id, qty);
        }

        TempData["Message"] = adjusted
            ? "Cart populated. Some items were adjusted for current stock/availability."
            : "Cart populated from your previous order.";
        return RedirectToAction("Index", "Cart");
    }
}
