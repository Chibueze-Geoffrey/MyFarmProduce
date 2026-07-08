using Microsoft.EntityFrameworkCore;
using MyFarmProduce.Common.Enums;
using MyFarmProduce.Domain.Entities;

namespace MyFarmProduce.Infrastructure.Data.Seed;

public static class SeedData
{
    public static void Apply(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Category>().HasData(
            new Category { Id = 1, Name = "Vegetables" },
            new Category { Id = 2, Name = "Tubers/Roots" },
            new Category { Id = 3, Name = "Grains/Legumes" },
            new Category { Id = 4, Name = "Fruits" },
            new Category { Id = 5, Name = "Proteins/Livestock" }
        );

        modelBuilder.Entity<Product>().HasData(
            // Vegetables
            Product(1, "Fresh Tomatoes", "Ripe red tomatoes", 1, ProductUnit.Basket, 8000m, 40),
            Product(2, "Spinach (Efo)", "Green leafy spinach", 1, ProductUnit.Bunch, 500m, 120),
            Product(3, "Bell Peppers", "Mixed colour bell peppers", 1, ProductUnit.Kg, 2500m, 60),
            Product(4, "Onions", "Red onions", 1, ProductUnit.Kg, 1800m, 100),

            // Tubers/Roots
            Product(5, "Yam Tuber", "Large white yam", 2, ProductUnit.Piece, 3500m, 80),
            Product(6, "Irish Potatoes", "Fresh Irish potatoes", 2, ProductUnit.Kg, 2200m, 90),
            Product(7, "Sweet Potatoes", "Orange-flesh sweet potatoes", 2, ProductUnit.Kg, 1500m, 70),
            Product(8, "Cassava", "Fresh cassava tubers", 2, ProductUnit.Kg, 900m, 150),

            // Grains/Legumes
            Product(9, "Local Rice", "Destoned local rice", 3, ProductUnit.Kg, 1700m, 200),
            Product(10, "Brown Beans", "Oloyin brown beans", 3, ProductUnit.Kg, 2100m, 130),
            Product(11, "White Maize", "Dried white maize", 3, ProductUnit.Kg, 1200m, 160),

            // Fruits
            Product(12, "Bananas", "Ripe bananas", 4, ProductUnit.Bunch, 1500m, 75),
            Product(13, "Pineapple", "Sweet pineapple", 4, ProductUnit.Piece, 1200m, 50),
            Product(14, "Watermelon", "Large watermelon", 4, ProductUnit.Piece, 2500m, 40),
            Product(15, "Oranges", "Juicy oranges", 4, ProductUnit.Basket, 6000m, 30),

            // Proteins/Livestock
            Product(16, "Live Chicken", "Broiler chicken", 5, ProductUnit.Piece, 9000m, 25),
            Product(17, "Catfish", "Fresh live catfish", 5, ProductUnit.Kg, 4000m, 60),
            Product(18, "Eggs", "Crate of eggs", 5, ProductUnit.Crate, 4500m, 45),
            Product(19, "Goat Meat", "Fresh goat meat", 5, ProductUnit.Kg, 6500m, 35)
        );
    }

    // HasData needs a fully-populated instance; StockQty is set through the
    // controlled SetInitialStock method so the private setter stays private.
    private static Product Product(
        int id, string name, string description, int categoryId,
        ProductUnit unit, decimal price, int stock)
    {
        var product = new Product
        {
            Id = id,
            Name = name,
            Description = description,
            CategoryId = categoryId,
            Unit = unit,
            Price = price,
            IsAvailable = true,
            ImageUrl = null
        };
        product.SetInitialStock(stock);
        return product;
    }
}
