using MyFarmProduce.Common.Enums;
using MyFarmProduce.Domain.Entities;

namespace MyFarmProduce.Application.Interfaces;

public interface IInventoryService
{
    Task<List<Product>> GetAllAsync();
    Task<Product?> GetByIdAsync(int id);
    Task<List<Category>> GetCategoriesAsync();

    Task<Product> CreateAsync(string name, string description, int categoryId,
        ProductUnit unit, decimal price, int stock, bool isAvailable, string? imageUrl);

    /// <summary>Updates editable fields. Stock is adjusted separately via <see cref="RestockAsync"/>.</summary>
    Task UpdateAsync(int id, string name, string description, int categoryId,
        ProductUnit unit, decimal price, bool isAvailable, string? imageUrl);

    Task RestockAsync(int id, int quantity);

    Task DeleteAsync(int id);
}
