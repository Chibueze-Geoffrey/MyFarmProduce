using MyFarmProduce.Common.Enums;

namespace MyFarmProduce.Domain.Entities;

public class Product
{
    public int Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;

    public int CategoryId { get; set; }
    public Category? Category { get; set; }

    public ProductUnit Unit { get; set; }
    public decimal Price { get; set; }

    // StockQty is intentionally read-only from the outside. The only ways to
    // change it are ReduceStock / Restock, which keeps the value consistent
    // and lets EF Core change-tracking observe every mutation correctly.
    public int StockQty { get; private set; }

    public bool IsAvailable { get; set; }
    public string? ImageUrl { get; set; }

    public ICollection<OrderItem> OrderItems { get; set; } = new List<OrderItem>();

    /// <summary>
    /// Decrease available stock by <paramref name="quantity"/>.
    /// Throws if the quantity is not positive or exceeds current stock.
    /// </summary>
    public void ReduceStock(int quantity)
    {
        if (quantity <= 0)
            throw new ArgumentOutOfRangeException(nameof(quantity), "Quantity must be greater than zero.");

        if (quantity > StockQty)
            throw new InvalidOperationException(
                $"Insufficient stock for '{Name}'. Available: {StockQty}, requested: {quantity}.");

        StockQty -= quantity;
    }

    /// <summary>
    /// Increase available stock by <paramref name="quantity"/>.
    /// Throws if the quantity is not positive.
    /// </summary>
    public void Restock(int quantity)
    {
        if (quantity <= 0)
            throw new ArgumentOutOfRangeException(nameof(quantity), "Quantity must be greater than zero.");

        StockQty += quantity;
    }

    /// <summary>
    /// Sets the initial stock level. Intended for entity creation / seeding only.
    /// </summary>
    public void SetInitialStock(int quantity)
    {
        if (quantity < 0)
            throw new ArgumentOutOfRangeException(nameof(quantity), "Initial stock cannot be negative.");

        StockQty = quantity;
    }
}
