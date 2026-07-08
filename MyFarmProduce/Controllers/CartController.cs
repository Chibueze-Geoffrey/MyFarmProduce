using Microsoft.AspNetCore.Mvc;
using MyFarmProduce.Web.Services;

namespace MyFarmProduce.Web.Controllers;

// User Story 3: cart add/adjust/remove, persists via session.
public class CartController : Controller
{
    private readonly ICartService _cart;
    private readonly CartFactory _factory;

    public CartController(ICartService cart, CartFactory factory)
    {
        _cart = cart;
        _factory = factory;
    }

    public async Task<IActionResult> Index() => View(await _factory.BuildAsync());

    [HttpPost, ValidateAntiForgeryToken]
    public IActionResult Add(int productId, int quantity = 1, string? returnUrl = null)
    {
        _cart.Add(productId, quantity);
        TempData["Message"] = "Added to cart.";
        if (!string.IsNullOrEmpty(returnUrl) && Url.IsLocalUrl(returnUrl))
            return Redirect(returnUrl);
        return RedirectToAction(nameof(Index));
    }

    [HttpPost, ValidateAntiForgeryToken]
    public IActionResult Update(int productId, int quantity)
    {
        _cart.SetQuantity(productId, quantity);
        return RedirectToAction(nameof(Index));
    }

    [HttpPost, ValidateAntiForgeryToken]
    public IActionResult Remove(int productId)
    {
        _cart.Remove(productId);
        return RedirectToAction(nameof(Index));
    }
}
