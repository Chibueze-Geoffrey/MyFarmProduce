using System.ComponentModel.DataAnnotations;

namespace MyFarmProduce.Web.Models;

public class AdminUserFormViewModel
{
    public int Id { get; set; }

    [Required, StringLength(150)]
    public string Name { get; set; } = string.Empty;

    [Required, EmailAddress, StringLength(256)]
    public string Email { get; set; } = string.Empty;

    [Required, Phone, StringLength(30)]
    public string Phone { get; set; } = string.Empty;

    [DataType(DataType.Password), StringLength(100, MinimumLength = 6)]
    [Display(Name = "Password (new users only)")]
    public string? Password { get; set; }
}
