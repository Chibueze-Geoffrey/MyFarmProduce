namespace MyFarmProduce.Application.Models;

/// <summary>A single line submitted from the cart when placing an order.</summary>
public record CartLineInput(int ProductId, int Quantity);
