using System.ComponentModel.DataAnnotations;

namespace MyFarmProduce.Web.Models;

public class CheckoutViewModel
{
    [Required, StringLength(500), Display(Name = "Delivery address")]
    public string DeliveryAddress { get; set; } = string.Empty;

    [Required, Phone, StringLength(30), Display(Name = "Phone number")]
    public string Phone { get; set; } = string.Empty;

    [StringLength(500), Display(Name = "Delivery note (optional)")]
    public string? DeliveryNote { get; set; }

    // Summary (populated for display).
    public decimal Subtotal { get; set; }
    public decimal DeliveryFee { get; set; }
    public decimal Total => Subtotal + DeliveryFee;
}
