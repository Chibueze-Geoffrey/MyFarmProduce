using MyFarmProduce.Domain.Entities;

namespace MyFarmProduce.Application.Interfaces;

public interface ICatalogService
{
    /// <summary>Categories with their available products, optionally filtered by a name keyword.</summary>
    Task<List<Category>> GetCategoriesWithProductsAsync(string? search = null);

    Task<Product?> GetProductByIdAsync(int id);

    Task<List<Product>> GetProductsByIdsAsync(IEnumerable<int> ids);
}
