using Microsoft.EntityFrameworkCore;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Common.Enums;
using MyFarmProduce.Domain.Entities;
using MyFarmProduce.Infrastructure.Data;

namespace MyFarmProduce.Infrastructure.Services;

public class InventoryService : IInventoryService
{
    private readonly AppDbContext _db;

    public InventoryService(AppDbContext db) => _db = db;

    public Task<List<Product>> GetAllAsync() =>
        _db.Products.Include(p => p.Category).OrderBy(p => p.Name).ToListAsync();

    public Task<Product?> GetByIdAsync(int id) =>
        _db.Products.FirstOrDefaultAsync(p => p.Id == id);

    public Task<List<Category>> GetCategoriesAsync() =>
        _db.Categories.OrderBy(c => c.Name).ToListAsync();

    public async Task<Product> CreateAsync(string name, string description, int categoryId,
        ProductUnit unit, decimal price, int stock, bool isAvailable, string? imageUrl)
    {
        var product = new Product
        {
            Name = name,
            Description = description,
            CategoryId = categoryId,
            Unit = unit,
            Price = price,
            IsAvailable = isAvailable,
            ImageUrl = imageUrl
        };
        product.SetInitialStock(stock);

        _db.Products.Add(product);
        await _db.SaveChangesAsync();
        return product;
    }

    public async Task UpdateAsync(int id, string name, string description, int categoryId,
        ProductUnit unit, decimal price, bool isAvailable, string? imageUrl)
    {
        var product = await _db.Products.FirstOrDefaultAsync(p => p.Id == id)
            ?? throw new InvalidOperationException($"Product {id} not found.");

        product.Name = name;
        product.Description = description;
        product.CategoryId = categoryId;
        product.Unit = unit;
        product.Price = price;
        product.IsAvailable = isAvailable;
        product.ImageUrl = imageUrl;

        await _db.SaveChangesAsync();
    }

    public async Task RestockAsync(int id, int quantity)
    {
        var product = await _db.Products.FirstOrDefaultAsync(p => p.Id == id)
            ?? throw new InvalidOperationException($"Product {id} not found.");

        product.Restock(quantity);
        await _db.SaveChangesAsync();
    }

    public async Task DeleteAsync(int id)
    {
        var product = await _db.Products.FirstOrDefaultAsync(p => p.Id == id);
        if (product is null) return;

        _db.Products.Remove(product);
        await _db.SaveChangesAsync();
    }
}
