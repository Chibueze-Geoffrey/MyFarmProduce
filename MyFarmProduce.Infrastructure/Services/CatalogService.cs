using Microsoft.EntityFrameworkCore;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Domain.Entities;
using MyFarmProduce.Infrastructure.Data;

namespace MyFarmProduce.Infrastructure.Services;

public class CatalogService : ICatalogService
{
    private readonly AppDbContext _db;

    public CatalogService(AppDbContext db) => _db = db;

    public async Task<List<Category>> GetCategoriesWithProductsAsync(string? search = null)
    {
        var products = _db.Products.AsQueryable();

        if (!string.IsNullOrWhiteSpace(search))
        {
            var term = search.Trim();
            products = products.Where(p => EF.Functions.Like(p.Name, $"%{term}%"));
        }

        var categories = await _db.Categories
            .OrderBy(c => c.Name)
            .ToListAsync();

        var grouped = await products
            .OrderBy(p => p.Name)
            .ToListAsync();

        foreach (var category in categories)
            category.Products = grouped.Where(p => p.CategoryId == category.Id).ToList();

        // When searching, hide categories that ended up with no matches.
        return string.IsNullOrWhiteSpace(search)
            ? categories
            : categories.Where(c => c.Products.Any()).ToList();
    }

    public Task<Product?> GetProductByIdAsync(int id) =>
        _db.Products.Include(p => p.Category).FirstOrDefaultAsync(p => p.Id == id);

    public async Task<List<Product>> GetProductsByIdsAsync(IEnumerable<int> ids)
    {
        var idList = ids.Distinct().ToList();
        return await _db.Products.Where(p => idList.Contains(p.Id)).ToListAsync();
    }
}
