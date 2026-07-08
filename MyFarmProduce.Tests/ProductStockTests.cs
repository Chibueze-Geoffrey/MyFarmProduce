using MyFarmProduce.Common.Enums;
using MyFarmProduce.Domain.Entities;
using Xunit;

namespace MyFarmProduce.Tests;

public class ProductStockTests
{
    private static Product NewProduct(int stock)
    {
        var p = new Product { Name = "Test", Unit = ProductUnit.Kg, Price = 100m };
        p.SetInitialStock(stock);
        return p;
    }

    [Fact]
    public void ReduceStock_DecrementsStock()
    {
        var p = NewProduct(10);
        p.ReduceStock(4);
        Assert.Equal(6, p.StockQty);
    }

    [Fact]
    public void ReduceStock_ExactStock_GoesToZero()
    {
        var p = NewProduct(5);
        p.ReduceStock(5);
        Assert.Equal(0, p.StockQty);
    }

    [Fact]
    public void ReduceStock_MoreThanAvailable_Throws()
    {
        var p = NewProduct(3);
        var ex = Assert.Throws<InvalidOperationException>(() => p.ReduceStock(4));
        Assert.Equal(3, p.StockQty); // unchanged
        Assert.Contains("Insufficient stock", ex.Message);
    }

    [Theory]
    [InlineData(0)]
    [InlineData(-1)]
    public void ReduceStock_NonPositive_Throws(int qty)
    {
        var p = NewProduct(10);
        Assert.Throws<ArgumentOutOfRangeException>(() => p.ReduceStock(qty));
        Assert.Equal(10, p.StockQty);
    }

    [Fact]
    public void Restock_IncrementsStock()
    {
        var p = NewProduct(10);
        p.Restock(5);
        Assert.Equal(15, p.StockQty);
    }

    [Theory]
    [InlineData(0)]
    [InlineData(-3)]
    public void Restock_NonPositive_Throws(int qty)
    {
        var p = NewProduct(10);
        Assert.Throws<ArgumentOutOfRangeException>(() => p.Restock(qty));
        Assert.Equal(10, p.StockQty);
    }
}
