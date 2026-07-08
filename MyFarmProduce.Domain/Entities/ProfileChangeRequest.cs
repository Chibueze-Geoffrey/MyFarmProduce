using MyFarmProduce.Common.Enums;

namespace MyFarmProduce.Domain.Entities;

/// <summary>
/// A customer's request to change a locked field (phone/email). Applied by an admin.
/// </summary>
public class ProfileChangeRequest
{
    public int Id { get; set; }

    public int CustomerId { get; set; }
    public Customer? Customer { get; set; }

    /// <summary>"Phone" or "Email".</summary>
    public string Field { get; set; } = string.Empty;
    public string CurrentValue { get; set; } = string.Empty;
    public string RequestedValue { get; set; } = string.Empty;

    public ChangeRequestStatus Status { get; set; } = ChangeRequestStatus.Pending;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime? ResolvedAt { get; set; }
    public string? AdminNote { get; set; }
}
