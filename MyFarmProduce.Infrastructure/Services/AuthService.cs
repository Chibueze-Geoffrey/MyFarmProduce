using Microsoft.EntityFrameworkCore;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Domain.Entities;
using MyFarmProduce.Infrastructure.Data;

namespace MyFarmProduce.Infrastructure.Services;

public class AuthService : IAuthService
{
    private readonly AppDbContext _db;
    private readonly IPasswordHasher _hasher;

    public AuthService(AppDbContext db, IPasswordHasher hasher)
    {
        _db = db;
        _hasher = hasher;
    }

    public async Task<Customer?> RegisterAsync(string name, string email, string phone, string password)
    {
        email = email.Trim().ToLowerInvariant();
        if (await _db.Customers.AnyAsync(c => c.Email == email))
            return null;

        var customer = new Customer
        {
            Name = name.Trim(),
            Email = email,
            Phone = phone.Trim(),
            PasswordHash = _hasher.Hash(password),
            IsAdmin = false
        };
        _db.Customers.Add(customer);
        await _db.SaveChangesAsync();
        return customer;
    }

    public async Task<Customer?> ValidateCredentialsAsync(string email, string password)
    {
        email = email.Trim().ToLowerInvariant();
        var customer = await _db.Customers.FirstOrDefaultAsync(c => c.Email == email);
        if (customer is null || !_hasher.Verify(customer.PasswordHash, password))
            return null;
        return customer;
    }

    public async Task ChangePasswordAsync(int customerId, string newPassword)
    {
        var customer = await _db.Customers.FirstOrDefaultAsync(c => c.Id == customerId)
            ?? throw new InvalidOperationException("Customer not found.");
        customer.PasswordHash = _hasher.Hash(newPassword);
        customer.MustChangePassword = false;
        await _db.SaveChangesAsync();
    }
}
