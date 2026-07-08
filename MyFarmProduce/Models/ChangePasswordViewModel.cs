using System.ComponentModel.DataAnnotations;

namespace MyFarmProduce.Web.Models;

public class ChangePasswordViewModel
{
    [Required, DataType(DataType.Password), StringLength(100, MinimumLength = 6)]
    [Display(Name = "New password")]
    public string NewPassword { get; set; } = string.Empty;

    [DataType(DataType.Password), Compare(nameof(NewPassword), ErrorMessage = "Passwords do not match.")]
    [Display(Name = "Confirm new password")]
    public string ConfirmPassword { get; set; } = string.Empty;
}
