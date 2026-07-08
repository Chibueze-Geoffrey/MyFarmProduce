using Microsoft.AspNetCore.Mvc;
using MyFarmProduce.Application.Interfaces;

namespace MyFarmProduce.Web.Controllers;

public class CatalogController : Controller
{
    private readonly ICatalogService _catalog;

    public CatalogController(ICatalogService catalog) => _catalog = catalog;

    // User Story 1: browse products grouped by category, with keyword search.
    public async Task<IActionResult> Index(string? q)
    {
        var categories = await _catalog.GetCategoriesWithProductsAsync(q);
        ViewData["Search"] = q;
        return View(categories);
    }

    // User Story 2: full product detail.
    public async Task<IActionResult> Details(int id)
    {
        var product = await _catalog.GetProductByIdAsync(id);
        if (product is null) return NotFound();
        return View(product);
    }
}
