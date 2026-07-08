namespace MyFarmProduce.Application.Models;

public class PlaceOrderRequest
{
    public string DeliveryAddress { get; set; } = string.Empty;
    public string Phone { get; set; } = string.Empty;
    public string? DeliveryNote { get; set; }
}
