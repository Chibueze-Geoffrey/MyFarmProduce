using System.ComponentModel.DataAnnotations;
using MyFarmProduce.Common.Enums;

namespace MyFarmProduce.Web.Models;

public class ProductFormViewModel
{
    public int Id { get; set; }

    [Required, StringLength(150)]
    public string Name { get; set; } = string.Empty;

    [StringLength(1000)]
    public string Description { get; set; } = string.Empty;

    [Display(Name = "Category")]
    public int CategoryId { get; set; }

    public ProductUnit Unit { get; set; }

    [Range(0, 10_000_000)]
    public decimal Price { get; set; }

    [Range(0, 1_000_000), Display(Name = "Initial stock")]
    public int StockQty { get; set; }

    [Display(Name = "Available for purchase")]
    public bool IsAvailable { get; set; } = true;

    [StringLength(500), Display(Name = "Image URL")]
    public string? ImageUrl { get; set; }
}
