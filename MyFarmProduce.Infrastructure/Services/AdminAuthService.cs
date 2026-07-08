using Microsoft.EntityFrameworkCore;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Domain.Entities;
using MyFarmProduce.Infrastructure.Data;

namespace MyFarmProduce.Infrastructure.Services;

public class AdminAuthService : IAdminAuthService
{
    private readonly AppDbContext _db;
    private readonly IPasswordHasher _hasher;

    public AdminAuthService(AppDbContext db, IPasswordHasher hasher)
    {
        _db = db;
        _hasher = hasher;
    }

    public async Task<Admin?> ValidateCredentialsAsync(string email, string password)
    {
        email = email.Trim().ToLowerInvariant();
        var admin = await _db.Admins.FirstOrDefaultAsync(a => a.Email == email);
        if (admin is null || !_hasher.Verify(admin.PasswordHash, password))
            return null;
        return admin;
    }

    public Task<Admin?> GetByIdAsync(int id) => _db.Admins.FirstOrDefaultAsync(a => a.Id == id);
}
