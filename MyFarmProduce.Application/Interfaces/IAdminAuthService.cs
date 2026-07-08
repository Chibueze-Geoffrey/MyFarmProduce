using MyFarmProduce.Domain.Entities;

namespace MyFarmProduce.Application.Interfaces;

public interface IAdminAuthService
{
    Task<Admin?> ValidateCredentialsAsync(string email, string password);
    Task<Admin?> GetByIdAsync(int id);
}
