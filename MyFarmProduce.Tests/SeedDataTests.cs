using Microsoft.EntityFrameworkCore;
using MyFarmProduce.Infrastructure.Data;
using Xunit;

namespace MyFarmProduce.Tests;

public class SeedDataTests
{
    private static AppDbContext NewContext()
    {
        var options = new DbContextOptionsBuilder<AppDbContext>()
            .UseInMemoryDatabase($"seed-{Guid.NewGuid()}")
            .Options;
        var ctx = new AppDbContext(options);
        ctx.Database.EnsureCreated(); // applies HasData seed
        return ctx;
    }

    [Fact]
    public void Seeds_FiveCategories()
    {
        using var ctx = NewContext();
        Assert.Equal(5, ctx.Categories.Count());
    }

    [Fact]
    public void Seeds_AtLeastThreeProductsPerCategory()
    {
        using var ctx = NewContext();
        var byCategory = ctx.Products
            .GroupBy(p => p.CategoryId)
            .Select(g => g.Count());

        Assert.All(byCategory, count => Assert.True(count >= 3));
        Assert.Equal(19, ctx.Products.Count());
    }

    [Fact]
    public void SeededProduct_HasStock()
    {
        using var ctx = NewContext();
        var product = ctx.Products.First();
        Assert.True(product.StockQty > 0);
    }
}
