using MyFarmProduce.Domain.Entities;

namespace MyFarmProduce.Web.Models;

public class CartLineViewModel
{
    public Product Product { get; set; } = default!;
    public int Quantity { get; set; }
    public decimal LineTotal => Product.Price * Quantity;
    public bool ExceedsStock => Quantity > Product.StockQty || !Product.IsAvailable;
}

public class CartViewModel
{
    public List<CartLineViewModel> Lines { get; set; } = new();
    public decimal Subtotal => Lines.Sum(l => l.LineTotal);
    public decimal DeliveryFee { get; set; }
    public decimal Total => Subtotal + DeliveryFee;
    public bool IsEmpty => Lines.Count == 0;
    public bool HasStockIssues => Lines.Any(l => l.ExceedsStock);
}
