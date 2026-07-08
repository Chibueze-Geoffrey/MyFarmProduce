using MyFarmProduce.Domain.Entities;

namespace MyFarmProduce.Application.Interfaces;

public interface IProfileService
{
    Task<Customer?> GetCustomerAsync(int customerId);

    /// <summary>Updates fields a customer may self-edit (name, photo). Phone/email are locked.</summary>
    Task UpdateCustomerProfileAsync(int customerId, string name, string? photoUrl);

    /// <summary>Files a request to change a locked field (Phone/Email) for admin approval.</summary>
    Task<ProfileChangeRequest> RequestFieldChangeAsync(int customerId, string field, string requestedValue);

    Task<List<ProfileChangeRequest>> GetMyChangeRequestsAsync(int customerId);

    Task<Admin?> GetAdminAsync(int adminId);
    Task UpdateAdminProfileAsync(int adminId, string name, string? photoUrl);
}
