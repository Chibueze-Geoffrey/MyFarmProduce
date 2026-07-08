using MyFarmProduce.Domain.Entities;

namespace MyFarmProduce.Application.Interfaces;

public interface IAuthService
{
    /// <summary>Registers a customer. Returns null if the email is already in use.</summary>
    Task<Customer?> RegisterAsync(string name, string email, string phone, string password);

    /// <summary>Returns the customer if credentials are valid, otherwise null.</summary>
    Task<Customer?> ValidateCredentialsAsync(string email, string password);
}
