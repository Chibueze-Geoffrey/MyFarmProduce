using MyFarmProduce.Domain.Entities;

namespace MyFarmProduce.Application.Interfaces;

/// <summary>Admin management of site users (customers) and their change requests.</summary>
public interface IUserAdminService
{
    Task<List<Customer>> GetCustomersAsync(string? search = null);
    Task<Customer?> GetCustomerAsync(int id);

    Task<Customer?> CreateCustomerAsync(string name, string email, string phone, string password);
    Task UpdateCustomerAsync(int id, string name, string email, string phone);
    Task DeleteCustomerAsync(int id);

    Task<List<ProfileChangeRequest>> GetChangeRequestsAsync(bool pendingOnly = true);
    Task ApproveChangeRequestAsync(int requestId, string? note);
    Task RejectChangeRequestAsync(int requestId, string? note);
}
