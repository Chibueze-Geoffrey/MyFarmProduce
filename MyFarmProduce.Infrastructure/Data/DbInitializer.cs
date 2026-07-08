using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Domain.Entities;

namespace MyFarmProduce.Infrastructure.Data;

public static class DbInitializer
{
    public const string AdminEmail = "admin@myfarmproduce.local";
    public const string AdminPassword = "Admin@123";

    /// <summary>Ensures default admin accounts exist in the Admins table (idempotent).</summary>
    public static async Task SeedAdminAsync(IServiceProvider services)
    {
        var db = services.GetRequiredService<AppDbContext>();
        var hasher = services.GetRequiredService<IPasswordHasher>();

        await EnsureAdminAsync(db, hasher, AdminEmail, "Store Admin", AdminPassword);
        await EnsureAdminAsync(db, hasher, "chibuezegeoffrey@gmail.com", "Chibueze Geoffrey", "Admin@123");

        await db.SaveChangesAsync();
    }

    private static async Task EnsureAdminAsync(AppDbContext db, IPasswordHasher hasher,
        string email, string name, string password)
    {
        email = email.Trim().ToLowerInvariant();
        if (await db.Admins.AnyAsync(a => a.Email == email))
            return;

        db.Admins.Add(new Admin
        {
            Name = name,
            Email = email,
            PasswordHash = hasher.Hash(password)
        });
    }
}
