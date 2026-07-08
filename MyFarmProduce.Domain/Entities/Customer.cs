namespace MyFarmProduce.Domain.Entities;

public class Customer
{
    public int Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string Phone { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string PasswordHash { get; set; } = string.Empty;
    public bool IsAdmin { get; set; }
    public string? PhotoUrl { get; set; }

    /// <summary>True until an admin-created user changes the default password on first login.</summary>
    public bool MustChangePassword { get; set; }

    public ICollection<Order> Orders { get; set; } = new List<Order>();
}
