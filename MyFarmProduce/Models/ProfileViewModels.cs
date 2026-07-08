using System.ComponentModel.DataAnnotations;
using MyFarmProduce.Domain.Entities;

namespace MyFarmProduce.Web.Models;

public class CustomerProfileViewModel
{
    public Customer Customer { get; set; } = default!;
    public List<ProfileChangeRequest> ChangeRequests { get; set; } = new();

    [Required, StringLength(150), Display(Name = "Display name")]
    public string Name { get; set; } = string.Empty;
}

public class FieldChangeRequestViewModel
{
    [Required]
    public string Field { get; set; } = "Phone"; // "Phone" or "Email"

    [Required, StringLength(256), Display(Name = "New value")]
    public string RequestedValue { get; set; } = string.Empty;
}
